package com.pet_project.watchstore.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Dạng phân trang ổn định trả về frontend: { content, number, size, totalElements, totalPages }. */
public record PageResponse<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
