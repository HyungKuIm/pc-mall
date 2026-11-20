package com.oraclejava.pcmall.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 장바구니 수량 변경 요청 (application/json).
 */
public record CartQuantityRequest(@NotNull @Min(1) @Max(Cart.MAX_QUANTITY) Integer quantity) {
}
