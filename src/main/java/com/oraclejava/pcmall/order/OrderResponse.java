package com.oraclejava.pcmall.order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 응답. 주문선의 제품 정보는 BLOB 을 제외한 요약만 담는다.
 */
public record OrderResponse(
		Long id,
		LocalDateTime orderDate,
		Long totalPrice,
		String formName,
		String formMail,
		String formMsg,
		List<Line> orderLines) {

	public record Line(Long id, Long productId, String productName, Integer quantity) {
	}

	static OrderResponse of(Order o, List<Line> lines) {
		return new OrderResponse(o.getId(), o.getOrderDate(), o.getTotalPrice(), o.getFormName(), o.getFormMail(),
				o.getFormMsg(), lines);
	}

}
