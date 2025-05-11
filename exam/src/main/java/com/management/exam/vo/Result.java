package com.management.exam.vo;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static Result<String> success() {
        Result<String> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData("");
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
} 