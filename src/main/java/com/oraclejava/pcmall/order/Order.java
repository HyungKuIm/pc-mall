package com.oraclejava.pcmall.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oraclejava.pcmall.customer.Customer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * 주문 (orders 테이블. ORDER 는 SQL 예약어라 복수형을 사용한다).
 * 결제 정보는 갖지 않는다 (실제 결제는 이뤄지지 않음).
 */
@Entity
@Table(name = "orders")
public class Order {

	/** 주문ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** 고객 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	/** 주문일자 */
	@Column(name = "order_date", nullable = false)
	private LocalDateTime orderDate;

	/** 총금액 (주문 시점의 제품 가격 × 수량 합계) */
	@Column(name = "total_price", nullable = false)
	private Long totalPrice;

	/** 주문선 목록 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "order_id", nullable = false)
	@OrderBy("id")
	private List<OrderLine> orderLines = new ArrayList<>();

	/** 받는이 이름 */
	@Column(name = "form_name", nullable = false, length = 100)
	private String formName;

	/** 받는이 메일 */
	@Column(name = "form_mail", nullable = false, length = 255)
	private String formMail;

	/** 받는이 정보 (주소, 연락처, 요청사항 등) */
	@Column(name = "form_msg", nullable = false, length = 1000)
	private String formMsg;

	protected Order() {
	}

	public Order(Customer customer, List<OrderLine> orderLines, Long totalPrice, String formName, String formMail,
			String formMsg) {
		this.customer = customer;
		this.orderDate = LocalDateTime.now();
		this.orderLines.addAll(orderLines);
		this.totalPrice = totalPrice;
		this.formName = formName;
		this.formMail = formMail;
		this.formMsg = formMsg;
	}

	public Long getId() {
		return id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public Long getTotalPrice() {
		return totalPrice;
	}

	public List<OrderLine> getOrderLines() {
		return Collections.unmodifiableList(orderLines);
	}

	public String getFormName() {
		return formName;
	}

	public String getFormMail() {
		return formMail;
	}

	public String getFormMsg() {
		return formMsg;
	}

}
