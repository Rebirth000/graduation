package com.management.exam.service.impl;

import com.management.exam.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.access-path:/api/uploads}")
    private String accessPath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public String uploadImage(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传的文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过5MB");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("只允许上传jpg、jpeg、png、gif格式的图片");
        }

        try {
            // 创建绝对路径的上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    throw new RuntimeException("无法创建上传目录: " + uploadDir.getAbsolutePath());
                }
                log.info("已创建上传目录: {}", uploadDir.getAbsolutePath());
            }

            // 生成唯一的文件名
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            File targetFile = new File(uploadDir, newFilename);

            // 保存文件
            file.transferTo(targetFile);
            log.info("文件已成功上传到: {}", targetFile.getAbsolutePath());

            // 返回文件访问路径
            return accessPath + "/" + newFilename;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
} 