package com.oraclejava.pcmall.cart;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * 장바구니. 로그인 세션마다 하나씩 존재하며 (제품ID → 수량) 만 보관한다.
 * 로그아웃(세션 무효화) 시 함께 사라지고, 주문이 완료되면 비워진다.
 */
@Component
@SessionScope
public class Cart implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 한 제품당 최대 수량 */
	public static final int MAX_QUANTITY = 99;

	private final Map<Long, Integer> items = new LinkedHashMap<>();

	/** 담기. 이미 담긴 제품이면 수량을 더한다. 합계가 최대 수량을 넘으면 false. */
	public synchronized boolean add(Long productId, int quantity) {
		int total = items.getOrDefault(productId, 0) + quantity;
		if (total > MAX_QUANTITY) {
			return false;
		}
		items.put(productId, total);
		return true;
	}

	/** 수량 변경. 담겨 있지 않은 제품이면 false. */
	public synchronized boolean changeQuantity(Long productId, int quantity) {
		if (!items.containsKey(productId)) {
			return false;
		}
		items.put(productId, quantity);
		return true;
	}

	/** 제품 빼기. 담겨 있지 않은 제품이면 false. */
	public synchronized boolean remove(Long productId) {
		return items.remove(productId) != null;
	}

	public synchronized void clear() {
		items.clear();
	}

	/** 담긴 순서를 유지한 복사본 */
	public synchronized Map<Long, Integer> snapshot() {
		return new LinkedHashMap<>(items);
	}

}
