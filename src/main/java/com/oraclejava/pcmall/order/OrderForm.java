package com.oraclejava.pcmall.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 주문 폼 (application/json). 주문할 제품은 장바구니 내용을 사용한다.
 */
public record OrderForm(
		@NotBlank @Size(max = 100) String formName,
		@NotBlank @Email @Size(max = 255) String formMail,
		@NotBlank @Size(max = 1000) String formMsg) {
}
