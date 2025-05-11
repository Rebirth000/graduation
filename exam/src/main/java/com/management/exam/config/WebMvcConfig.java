package com.management.exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.access-path:/api/uploads}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径，使用绝对路径
        String pathPattern = accessPath + "/**";
        String location = "file:" + uploadPath + "/";
        registry.addResourceHandler(pathPattern)
                .addResourceLocations(location);
    }
} 