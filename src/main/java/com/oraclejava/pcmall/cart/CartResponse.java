package com.oraclejava.pcmall.cart;

import java.util.List;

/**
 * 장바구니 응답.
 */
public record CartResponse(List<Item> items, long totalPrice) {

	public record Item(Long productId, String productName, Integer price, int quantity, long subtotal) {
	}

}
