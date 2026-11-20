package com.oraclejava.pcmall.order;

/**
 * 주문선 조회용 행 (JPQL 생성자 표현식 결과).
 */
public record OrderLineRow(Long orderId, Long id, Long productId, String productName, Integer quantity) {

	OrderResponse.Line toLine() {
		return new OrderResponse.Line(id, productId, productName, quantity);
	}

}
