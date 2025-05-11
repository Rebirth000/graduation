package com.management.exam.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.management.exam.dto.ExamExportDTO;
import com.management.exam.dto.ExamHistoryQueryDTO;
import com.management.exam.entity.QuestionOption;
import com.management.exam.entity.Student;
import com.management.exam.enums.QuestionType;
import com.management.exam.mapper.ExamMapper;
import com.management.exam.mapper.StudentMapper;
import com.management.exam.service.ExamHistoryService;
import com.management.exam.vo.ExamAnswerDetailVO;
import com.management.exam.vo.ExamHistoryVO;
import com.management.exam.vo.PageResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamHistoryServiceImpl implements ExamHistoryService {
    
    private final ExamMapper examMapper;
    private final StudentMapper studentMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int IMAGE_HEIGHT_CELLS = 10; // 图片显示高度(行数)
    private static final int IMAGE_WIDTH_CELLS = 2; // 图片显示宽度(列数)

    @Override
    public PageResponse<ExamHistoryVO> getStudentExamHistory(Integer studentId, ExamHistoryQueryDTO query) {
        // 计算分页参数
        int offset = (query.getPageNum() - 1) * query.getPageSize();
        
        // 获取总记录数
        long total = examMapper.countStudentExams(studentId, query);
        
        // 获取分页数据
        List<ExamHistoryVO> records = examMapper.findStudentExams(studentId, query, offset, query.getPageSize());
        
        return new PageResponse<>(records, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public List<ExamAnswerDetailVO> getStudentExamDetail(Integer studentId, Integer examId) {
        List<ExamAnswerDetailVO> answers = examMapper.findExamAnswerDetail(studentId, examId);
        
        // 按order_num排序
        answers.sort((a, b) -> {
            // 处理null情况
            if (a.getOrderNum() == null && b.getOrderNum() == null) {
                return 0;
            }
            if (a.getOrderNum() == null) {
                return 1;
            }
            if (b.getOrderNum() == null) {
                return -1;
            }
            return a.getOrderNum().compareTo(b.getOrderNum());
        });
        
        return answers;
    }
    
    @Override
    public void exportExamAnswersExcel(ExamExportDTO exportDTO, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            
            // 处理每条记录
            for (ExamExportDTO.ExamRecordDTO record : exportDTO.getExamRecords()) {
                Integer examId = record.getExamId();
                Integer studentId = record.getStudentId();
                
                // 查询学生信息
                Student student = studentMapper.selectById(studentId);
                if (student == null) {
                    continue;
                }
                
                // 查询考试历史记录
                List<ExamHistoryVO> examHistories = examMapper.findStudentExams(
                    studentId, 
                    new ExamHistoryQueryDTO(), 
                    0, 
                    1
                );
                
                if (examHistories.isEmpty()) {
                    continue;
                }
                
                ExamHistoryVO examHistory = examHistories.get(0);
                
                // 查询答题详情并按顺序排序
                List<ExamAnswerDetailVO> allAnswers = examMapper.findExamAnswerDetail(studentId, examId);
                
                // 按题目顺序排序（通过order_num字段）
                allAnswers.sort((a, b) -> {
                    // 处理null情况
                    if (a.getOrderNum() == null && b.getOrderNum() == null) {
                        return 0;
                    }
                    if (a.getOrderNum() == null) {
                        return 1;
                    }
                    if (b.getOrderNum() == null) {
                        return -1;
                    }
                    return a.getOrderNum().compareTo(b.getOrderNum());
                });
                
                // 过滤题型
                allAnswers = allAnswers.stream()
                    .filter(answer -> exportDTO.getQuestionTypes().contains(answer.getType()))
                    .toList();
                
                if (allAnswers.isEmpty()) {
                    continue;
                }
                
                // 按题型分组
                Map<String, List<ExamAnswerDetailVO>> answersByType = new HashMap<>();
                for (String type : exportDTO.getQuestionTypes()) {
                    answersByType.put(type, new ArrayList<>());
                }
                
                for (ExamAnswerDetailVO answer : allAnswers) {
                    answersByType.computeIfAbsent(answer.getType(), k -> new ArrayList<>()).add(answer);
                }
                
                // 创建该学生的工作表 - 使用学生姓名和考试名称
                String sheetName = student.getName() + "-" + examHistory.getExamName();
                // 确保工作表名称有效 (不超过31个字符且不包含特殊字符)
                sheetName = sanitizeSheetName(sheetName);
                Sheet sheet = workbook.createSheet(sheetName);
                
                // 设置列宽
                sheet.setColumnWidth(0, 10 * 256);  // 题号
                sheet.setColumnWidth(1, 35 * 256);  // 学生答案
                sheet.setColumnWidth(2, 80 * 256);  // 图片列宽增加
                
                // 创建表头
                Row headerRow = sheet.createRow(0);
                Cell titleCell = headerRow.createCell(0);
                titleCell.setCellValue(examHistory.getExamName() + " - " + student.getName() + " 答卷");
                titleCell.setCellStyle(headerStyle);
                // 合并单元格作为标题
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
                
                int rowIndex = 1;
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                
                // 定义题型顺序
                String[] typeOrder = {
                    "SINGLE_CHOICE",   // 单选题
                    "MULTIPLE_CHOICE", // 多选题
                    "TRUE_FALSE",      // 判断题
                    "FILL_BLANK",      // 填空题
                    "SHORT_ANSWER",    // 简答题
                    "ANALYSIS"         // 分析题
                };
                
                // 按题型顺序导出
                for (String type : typeOrder) {
                    List<ExamAnswerDetailVO> answers = answersByType.get(type);
                    if (answers == null || answers.isEmpty()) {
                        continue;
                    }
                    
                    // 添加题型标题
                    Row typeRow = sheet.createRow(rowIndex++);
                    Cell typeCell = typeRow.createCell(0);
                    typeCell.setCellValue(getQuestionTypeText(type));
                    typeCell.setCellStyle(subHeaderStyle);
                    // 合并单元格作为题型标题
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex-1, rowIndex-1, 0, 2));
                    
                    // 添加字段标题
                    Row fieldRow = sheet.createRow(rowIndex++);
                    fieldRow.createCell(0).setCellValue("题号");
                    fieldRow.createCell(1).setCellValue("学生答案");
                    if (isNonChoiceType(type)) {
                        fieldRow.createCell(2).setCellValue("图片");
                    }
                    
                    // 设置字段标题样式
                    for (int i = 0; i < 3; i++) {
                        if (fieldRow.getCell(i) != null) {
                            fieldRow.getCell(i).setCellStyle(subHeaderStyle);
                        }
                    }
                    
                    // 导出该题型的所有题目
                    for (int i = 0; i < answers.size(); i++) {
                        ExamAnswerDetailVO answer = answers.get(i);
                        Row row = sheet.createRow(rowIndex++);
                        
                        // 设置行高
                        row.setHeightInPoints(80);
                        
                        // 题号 (第几题)
                        row.createCell(0).setCellValue(i + 1);
                        
                        // 学生答案
                        Cell answerCell = row.createCell(1);
                        String answerText = processAnswerForExport(answer);
                        answerCell.setCellValue(answerText);
                        
                        // 图片处理 (仅当是非选择题型且有图片)
                        if (isNonChoiceType(type) && 
                            answer.getAnswerImages() != null && 
                            !answer.getAnswerImages().isEmpty()) {
                            
                            List<String> imageUrls = answer.getAnswerImages();
                            
                            // 创建图片单元格
                            Cell imageCell = row.createCell(2);
                            
                            // 如果有多张图片，显示数量提示
                            if (imageUrls.size() > 0) {
                                imageCell.setCellValue("共 " + imageUrls.size() + " 张图片");
                                
                                // 设置行高以适应多张图片
                                row.setHeightInPoints(Math.max(150, imageUrls.size() * 30)); // 增加行高，以便更好地显示图片
                                
                                // 处理所有图片
                                for (int imgIndex = 0; imgIndex < imageUrls.size(); imgIndex++) {
                                    try {
                                        String imageUrl = imageUrls.get(imgIndex);
                                        byte[] imageData = downloadImage(imageUrl);
                                        
                                        if (imageData != null && imageData.length > 0) {
                                            int pictureIdx = workbook.addPicture(imageData, Workbook.PICTURE_TYPE_JPEG);
                                            
                                            // 计算图片在单元格中的位置
                                            // 水平排列多张图片
                                            int imgLeft = imgIndex * 160;  // 增加每张图片的水平间距
                                            
                                            // 创建锚点 - 水平排列图片
                                            XSSFClientAnchor anchor = new XSSFClientAnchor(
                                                imgLeft * 10000, 0, // dx1, dy1 (转换为EMU单位)
                                                (imgLeft + 150) * 10000, 300000, // dx2, dy2，增加图片宽度和高度
                                                (short) 2, rowIndex - 1, // col1, row1
                                                (short) 3, rowIndex // col2, row2
                                            );
                                            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                                            
                                            // 添加图片到绘图对象
                                            Picture picture = ((XSSFDrawing) drawing).createPicture(anchor, pictureIdx);
                                            
                                            // 调整图片大小但保持比例
                                            double scale = 0.5; // 增加缩放比例以显示更大的图片
                                            picture.resize(scale);
                                        }
                                    } catch (Exception e) {
                                        log.error("处理图片时出错: " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                    
                    // 在不同题型之间添加空行
                    rowIndex++;
                }
            }
            
            // 设置响应头
            String filename = "答卷导出.xlsx";
            if (exportDTO.getExamRecords().size() == 1) {
                // 使用"学生姓名+考试名称"作为文件名
                ExamExportDTO.ExamRecordDTO record = exportDTO.getExamRecords().get(0);
                Student student = studentMapper.selectById(record.getStudentId());
                List<ExamHistoryVO> examHistories = examMapper.findStudentExams(
                    record.getStudentId(), 
                    new ExamHistoryQueryDTO(), 
                    0, 
                    1
                );
                
                if (student != null && !examHistories.isEmpty()) {
                    filename = student.getName() + "-" + examHistories.get(0).getExamName() + ".xlsx";
                        }
                    } else {
                // 多个考生时使用默认文件名
                filename = "多名考生答卷导出.xlsx";
            }
            
            // 设置文件名编码，处理中文文件名
            filename = encodeFilename(filename);
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            
            // 写入响应
            workbook.write(response.getOutputStream());
            
        } catch (IOException e) {
            log.error("导出Excel失败: " + e.getMessage(), e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }
    
    /**
     * 编码文件名以处理中文字符
     */
    private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return filename;
        }
    }
    
    /**
     * 清理工作表名称，确保有效
     */
    private String sanitizeSheetName(String name) {
        // Excel工作表名称最多31个字符
        if (name.length() > 31) {
            name = name.substring(0, 31);
        }
        
        // 替换无效字符
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        return name;
    }
    
    /**
     * 创建子标题样式
     */
    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 判断是否为非选择题型
     */
    private boolean isNonChoiceType(String type) {
        return "FILL_BLANK".equals(type) || 
               "SHORT_ANSWER".equals(type) || 
               "ANALYSIS".equals(type);
    }
    
    /**
     * 下载图片数据
     */
    private byte[] downloadImage(String imageUrl) {
        try {
            // 处理相对路径URL
            if (imageUrl.startsWith("/api/")) {
                // 这里需要替换为实际的基础URL，比如从配置中读取
                String baseUrl = "http://localhost:8080"; // 替换为您的实际后端地址
                imageUrl = baseUrl + imageUrl;
            }
            
            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (InputStream inputStream = conn.getInputStream()) {
                return IOUtils.toByteArray(inputStream);
            }
        } catch (Exception e) {
            log.error("下载图片失败: " + imageUrl, e);
            return null;
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private String getQuestionTypeText(String type) {
        return switch (type) {
            case "SINGLE_CHOICE" -> "单选题";
            case "MULTIPLE_CHOICE" -> "多选题";
            case "FILL_BLANK" -> "填空题";
            case "TRUE_FALSE" -> "判断题";
            case "SHORT_ANSWER" -> "简答题";
            case "ANALYSIS" -> "分析题";
            default -> type;
        };
    }
    
    private String processTitleForExport(ExamAnswerDetailVO answer) {
        String title = answer.getTitle();
        try {
            // 尝试解析JSON格式的题目
            if (answer.getType().equals("FILL_BLANK")) {
                try {
                    JSONObject json = JSON.parseObject(title);
                    if (json.containsKey("prefix") && json.containsKey("suffix")) {
                        return json.getString("prefix") + "____" + json.getString("suffix");
                    } else if (json.containsKey("content") && json.containsKey("type") && "multi-blanks".equals(json.getString("type"))) {
                        return json.getString("content").replace("[blank]", "____");
                    }
                } catch (Exception e) {
                    // 解析失败，使用原始内容
                }
            }
        } catch (Exception e) {
            log.warn("处理题目内容时出错", e);
        }
        return title;
    }
    
    private String processAnswerForExport(ExamAnswerDetailVO answer) {
        if (answer.getAnswer() == null) {
            return "未作答";
        }
        
        try {
            // 根据题型处理答案
            switch (answer.getType()) {
                case "SINGLE_CHOICE":
                    // 单选题，从选项中找到学生选择的答案
                    String optionCode = JSON.parseObject(answer.getAnswer()).toString();
                    String options = answer.getOptions();
                    List<QuestionOption> optionList = JSON.parseArray(options, QuestionOption.class);
                    for (QuestionOption option : optionList) {
                        if (option.getCode().equals(optionCode)) {
                            return option.getCode() + ". " + option.getContent();
                        }
                    }
                    return optionCode; // 未找到对应选项时返回选项代码
                    
                case "MULTIPLE_CHOICE":
                    // 多选题，从选项中找到学生选择的所有答案
                    List<String> selectedCodes = JSON.parseArray(answer.getAnswer(), String.class);
                    List<QuestionOption> multiOptions = JSON.parseArray(answer.getOptions(), QuestionOption.class);
                    List<String> selectedTexts = new ArrayList<>();
                    for (String code : selectedCodes) {
                        for (QuestionOption option : multiOptions) {
                            if (option.getCode().equals(code)) {
                                selectedTexts.add(option.getCode() + ". " + option.getContent());
                                break;
                            }
                        }
                    }
                    return String.join("; ", selectedTexts);
                    
                case "TRUE_FALSE":
                    // 判断题
                    boolean value = "true".equals(JSON.parseObject(answer.getAnswer()).toString());
                    return value ? "正确" : "错误";
                    
                case "FILL_BLANK":
                case "SHORT_ANSWER":
                case "ANALYSIS":
                    // 填空题、简答题、分析题直接返回答案内容
                    return answer.getAnswer();
                    
                default:
                    return answer.getAnswer();
            }
        } catch (Exception e) {
            log.warn("处理答案内容时出错", e);
            return answer.getAnswer();
        }
    }
} 