package com.management.exam.controller;

import com.management.exam.exception.ResourceNotFoundException;
import com.management.exam.vo.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception ex) {
        ex.printStackTrace();
        return Result.error("系统错误，请稍后再试。");
    }
} 