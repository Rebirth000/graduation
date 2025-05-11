package com.management.exam.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传图片文件
     * @param file 图片文件
     * @return 图片访问路径
     */
    String uploadImage(MultipartFile file);
} 