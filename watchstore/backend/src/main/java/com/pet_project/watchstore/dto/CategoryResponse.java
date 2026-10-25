package com.pet_project.watchstore.dto;

import com.pet_project.watchstore.entity.Category;

public record CategoryResponse(Integer id, String name) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
