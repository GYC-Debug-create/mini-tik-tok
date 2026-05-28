package com.minitiktok.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> of(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> ok(T data) {
        return of(200, "success", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return of(200, message, data);
    }

    public static <T> Result<T> created(String message, T data) {
        return of(201, message, data);
    }

    public static <T> Result<T> fail(int code, String message) {
        return of(code, message, null);
    }
}
