package com.management.exam.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageInitializer implements CommandLineRunner {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void run(String... args) throws Exception {
        // 确保上传目录存在（使用绝对路径）
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            try {
                boolean created = uploadDir.mkdirs();
                if (created) {
                    log.info("创建文件上传目录成功: {}", uploadDir.getAbsolutePath());
                } else {
                    log.error("创建文件上传目录失败: {}", uploadDir.getAbsolutePath());
                }
            } catch (Exception e) {
                log.error("创建文件上传目录失败", e);
            }
        } else {
            log.info("文件上传目录已存在: {}", uploadDir.getAbsolutePath());
        }
    }
} 