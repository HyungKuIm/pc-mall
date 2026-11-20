package com.oraclejava.pcmall.order;

import com.oraclejava.pcmall.product.PcProduct;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 주문선 (order_line 테이블). 주문 1건에 포함된 제품 1종과 그 수량.
 * 소속 주문(order_id)은 {@link Order#getOrderLines()} 쪽에서 관리한다.
 */
@Entity
@Table(name = "order_line")
public class OrderLine {

	/** 주문선ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** 제품 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private PcProduct product;

	/** 수량 */
	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	protected OrderLine() {
	}

	public OrderLine(PcProduct product, Integer quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public PcProduct getProduct() {
		return product;
	}

	public Integer getQuantity() {
		return quantity;
	}

}
