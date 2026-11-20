package com.oraclejava.pcmall.cart;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 장바구니 API (로그인 필요). 장바구니는 로그인 세션에 보관된다.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService service;

	public CartController(CartService service) {
		this.service = service;
	}

	/** 장바구니 조회 */
	@GetMapping
	public CartResponse view() {
		return service.view();
	}

	/** 담기 (이미 담긴 상품이면 수량 추가) */
	@PostMapping("/items")
	public CartResponse add(@Validated @RequestBody CartItemRequest request) {
		return service.add(request);
	}

	/** 수량 변경 */
	@PutMapping("/items/{productId}")
	public CartResponse changeQuantity(@PathVariable Long productId,
			@Validated @RequestBody CartQuantityRequest request) {
		return service.changeQuantity(productId, request.quantity());
	}

	/** 상품 빼기 */
	@DeleteMapping("/items/{productId}")
	public CartResponse remove(@PathVariable Long productId) {
		return service.remove(productId);
	}

	/** 비우기 */
	@DeleteMapping
	public ResponseEntity<Void> clear() {
		service.clear();
		return ResponseEntity.noContent().build();
	}

}
