package com.oraclejava.pcmall.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 담기 요청 (application/json).
 */
public record CartItemRequest(
		@NotNull Long productId,
		@NotNull @Min(1) @Max(Cart.MAX_QUANTITY) Integer quantity) {
}
