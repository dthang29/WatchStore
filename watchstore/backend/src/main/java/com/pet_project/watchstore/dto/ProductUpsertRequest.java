package com.pet_project.watchstore.dto;

import com.pet_project.watchstore.common.constants.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Payload dùng chung khi admin tạo hoặc cập nhật sản phẩm. */
public record ProductUpsertRequest(
        @NotNull(message = "Vui lòng chọn danh mục")
        Integer categoryId,

        @NotBlank(message = "Vui lòng nhập tên sản phẩm")
        @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
        String name,

        @NotBlank(message = "Vui lòng nhập thương hiệu")
        @Size(max = 100, message = "Thương hiệu tối đa 100 ký tự")
        String brand,

        String description,

        @NotNull(message = "Vui lòng chọn loại máy")
        MovementType movementType,

        @NotNull(message = "Vui lòng nhập giá")
        @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
        BigDecimal price,

        @NotNull(message = "Vui lòng nhập tồn kho")
        @PositiveOrZero(message = "Tồn kho không được âm")
        Integer stock,

        @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
        String imageUrl
) {
}
