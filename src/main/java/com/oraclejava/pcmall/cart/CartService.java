package com.oraclejava.pcmall.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.oraclejava.pcmall.product.PcProductRepository;
import com.oraclejava.pcmall.product.PcProductResponse;

@Service
@Transactional(readOnly = true)
public class CartService {

	private final Cart cart;

	private final PcProductRepository productRepository;

	public CartService(Cart cart, PcProductRepository productRepository) {
		this.cart = cart;
		this.productRepository = productRepository;
	}

	public CartResponse view() {
		return toResponse(cart.snapshot());
	}

	public CartResponse add(CartItemRequest request) {
		if (!productRepository.existsById(request.productId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다. id=" + request.productId());
		}
		if (!cart.add(request.productId(), request.quantity())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"한 상품은 최대 " + Cart.MAX_QUANTITY + "개까지 담을 수 있습니다.");
		}
		return view();
	}

	public CartResponse changeQuantity(Long productId, int quantity) {
		if (!cart.changeQuantity(productId, quantity)) {
			throw notInCart(productId);
		}
		return view();
	}

	public CartResponse remove(Long productId) {
		if (!cart.remove(productId)) {
			throw notInCart(productId);
		}
		return view();
	}

	public void clear() {
		cart.clear();
	}

	/**
	 * 장바구니 내용을 현재 상품 정보로 계산한다. 그 사이 삭제된 상품은 장바구니에서 뺀다.
	 */
	private CartResponse toResponse(Map<Long, Integer> items) {
		if (items.isEmpty()) {
			return new CartResponse(List.of(), 0);
		}
		Map<Long, PcProductResponse> products = productRepository.findSummariesByIdIn(items.keySet())
			.stream()
			.collect(Collectors.toMap(PcProductResponse::id, Function.identity()));

		List<CartResponse.Item> lines = new ArrayList<>();
		long total = 0;
		for (Map.Entry<Long, Integer> e : items.entrySet()) {
			PcProductResponse p = products.get(e.getKey());
			if (p == null) {
				cart.remove(e.getKey());
				continue;
			}
			long subtotal = (long) p.price() * e.getValue();
			total += subtotal;
			lines.add(new CartResponse.Item(p.id(), p.name(), p.price(), e.getValue(), subtotal));
		}
		return new CartResponse(lines, total);
	}

	private static ResponseStatusException notInCart(Long productId) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "장바구니에 없는 상품입니다. id=" + productId);
	}

}
