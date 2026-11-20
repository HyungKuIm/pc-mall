package com.oraclejava.pcmall.order;

/**
 * 주문 완료 응답 (확인 메시지 + 주문 내용).
 */
public record OrderCompleteResponse(String message, OrderResponse order) {

	static final String MESSAGE = "주문이 접수되었습니다. (실제 결제는 이뤄지지 않습니다.)";

	static OrderCompleteResponse of(OrderResponse order) {
		return new OrderCompleteResponse(MESSAGE, order);
	}

}
