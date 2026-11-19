package com.oraclejava.pcmall.product;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 상품 등록/수정 요청 (multipart/form-data).
 */
public record PcProductForm(
		@NotBlank @Size(max = 200) String name,
		@NotNull @PositiveOrZero Integer price,
		@Size(max = 50) String kind,
		@Size(max = 4000) String text,
		MultipartFile picS,
		MultipartFile picL) {
}
