package com.oraclejava.pcmall.order;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oraclejava.pcmall.cart.Cart;
import com.oraclejava.pcmall.security.CustomerUserDetails;

/**
 * 주문 API (로그인 필요). 장바구니에 담은 상품을 주문 폼과 함께 주문한다.
 * 주문만 접수하며 실제 결제는 이뤄지지 않는다.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService service;

	private final Cart cart;

	public OrderController(OrderService service, Cart cart) {
		this.service = service;
		this.cart = cart;
	}

	/** 주문하기 (장바구니 내용 + 주문 폼). 성공 시 확인 메시지를 돌려주고 장바구니를 비운다. */
	@PostMapping
	public ResponseEntity<OrderCompleteResponse> placeOrder(@AuthenticationPrincipal CustomerUserDetails user,
			@Validated @RequestBody OrderForm form) {
		OrderResponse order = service.placeOrder(user.getId(), cart.snapshot(), form);
		cart.clear();
		return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(OrderCompleteResponse.of(order));
	}

	/** 내 주문 목록 */
	@GetMapping
	public List<OrderResponse> myOrders(@AuthenticationPrincipal CustomerUserDetails user) {
		return service.findMyOrders(user.getId());
	}

	/** 내 주문 단건 */
	@GetMapping("/{id}")
	public OrderResponse myOrder(@AuthenticationPrincipal CustomerUserDetails user, @PathVariable Long id) {
		return service.findMyOrder(user.getId(), id);
	}

}
