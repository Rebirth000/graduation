package com.management.exam.controller;

import com.management.exam.service.FileService;
import com.management.exam.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileService.uploadImage(file);
        return Result.success(imageUrl);
    }
} 