package com.oraclejava.pcmall.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.oraclejava.pcmall.customer.CustomerRepository;
import com.oraclejava.pcmall.product.PcProductRepository;
import com.oraclejava.pcmall.product.PcProductResponse;

@Service
@Transactional(readOnly = true)
public class OrderService {

	private final OrderRepository orderRepository;

	private final PcProductRepository productRepository;

	private final CustomerRepository customerRepository;

	public OrderService(OrderRepository orderRepository, PcProductRepository productRepository,
			CustomerRepository customerRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.customerRepository = customerRepository;
	}

	/**
	 * 주문 등록. 장바구니 항목(제품ID → 수량)과 주문 폼으로 주문을 만든다.
	 * 총금액은 현재 제품 가격으로 서버에서 계산한다. 결제 처리는 하지 않는다.
	 */
	@Transactional
	public OrderResponse placeOrder(Long customerId, Map<Long, Integer> cartItems, OrderForm form) {
		if (cartItems.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "장바구니가 비어 있습니다.");
		}
		Map<Long, PcProductResponse> products = productRepository.findSummariesByIdIn(cartItems.keySet())
			.stream()
			.collect(Collectors.toMap(PcProductResponse::id, Function.identity()));

		List<OrderLine> lines = new ArrayList<>();
		long total = 0;
		for (Map.Entry<Long, Integer> e : cartItems.entrySet()) {
			PcProductResponse p = products.get(e.getKey());
			if (p == null) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"판매가 종료된 상품이 장바구니에 있습니다. 장바구니를 확인해 주세요. id=" + e.getKey());
			}
			total = Math.addExact(total, Math.multiplyExact((long) p.price(), e.getValue()));
			lines.add(new OrderLine(productRepository.getReferenceById(p.id()), e.getValue()));
		}

		Order order = new Order(customerRepository.getReferenceById(customerId), lines, total,
				form.formName().trim(), form.formMail().trim(), form.formMsg().trim());
		orderRepository.save(order);

		List<OrderResponse.Line> lineResponses = order.getOrderLines()
			.stream()
			.map(l -> {
				PcProductResponse p = products.get(l.getProduct().getId());
				return new OrderResponse.Line(l.getId(), p.id(), p.name(), l.getQuantity());
			})
			.toList();
		return OrderResponse.of(order, lineResponses);
	}

	/** 내 주문 목록 (최근 주문부터) */
	public List<OrderResponse> findMyOrders(Long customerId) {
		List<Order> orders = orderRepository.findByCustomerIdOrderByIdDesc(customerId);
		if (orders.isEmpty()) {
			return List.of();
		}
		Map<Long, List<OrderResponse.Line>> lines = linesByOrderId(orders.stream().map(Order::getId).toList());
		return orders.stream().map(o -> OrderResponse.of(o, lines.getOrDefault(o.getId(), List.of()))).toList();
	}

	/** 내 주문 단건. 다른 고객의 주문은 404. */
	public OrderResponse findMyOrder(Long customerId, Long orderId) {
		Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다. id=" + orderId));
		return OrderResponse.of(order, linesByOrderId(List.of(orderId)).getOrDefault(orderId, List.of()));
	}

	private Map<Long, List<OrderResponse.Line>> linesByOrderId(List<Long> orderIds) {
		return orderRepository.findLineRows(orderIds)
			.stream()
			.collect(Collectors.groupingBy(OrderLineRow::orderId,
					Collectors.mapping(OrderLineRow::toLine, Collectors.toList())));
	}

}
