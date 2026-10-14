package com.sinnarmobile;

import java.util.Map;
import org.springframework.data.domain.Page;

public record Api<T>(boolean success, T data, String message) {
    public static <T> Api<T> ok(T data) { return new Api<>(true, data, null); }
    public static Api<Void> done(String message) { return new Api<>(true, null, message); }
    public static Api<Object> error(String message, Object errors) { return new Api<>(false, errors, message); }
    public static <T> Api<Map<String,Object>> page(Page<T> page) {
        return ok(Map.of("items", page.getContent(), "page", page.getNumber(), "size", page.getSize(),
            "totalElements", page.getTotalElements(), "totalPages", page.getTotalPages()));
    }
}
