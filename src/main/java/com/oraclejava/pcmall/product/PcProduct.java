package com.oraclejava.pcmall.product;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * 중고 노트북 상품 (pc_product 테이블).
 */
@Entity
@Table(name = "pc_product")
public class PcProduct {

	/** 제품ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** 제품명 */
	@Column(name = "name", nullable = false, length = 200)
	private String name;

	/** 가격 */
	@Column(name = "price", nullable = false)
	private Integer price;

	/** 종류 */
	@Column(name = "kind", length = 50)
	private String kind;

	/** 설명 */
	@Column(name = "text", length = 4000)
	private String text;

	/** 작은 이미지 (BLOB) */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "pic_s", columnDefinition = "LONGBLOB")
	private byte[] picS;

	/** 큰 이미지 (BLOB) */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "pic_l", columnDefinition = "LONGBLOB")
	private byte[] picL;

	protected PcProduct() {
	}

	public PcProduct(String name, Integer price, String kind, String text) {
		this.name = name;
		this.price = price;
		this.kind = kind;
		this.text = text;
	}

	public void update(String name, Integer price, String kind, String text) {
		this.name = name;
		this.price = price;
		this.kind = kind;
		this.text = text;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getPrice() {
		return price;
	}

	public String getKind() {
		return kind;
	}

	public String getText() {
		return text;
	}

	public byte[] getPicS() {
		return picS;
	}

	public void setPicS(byte[] picS) {
		this.picS = picS;
	}

	public byte[] getPicL() {
		return picL;
	}

	public void setPicL(byte[] picL) {
		this.picL = picL;
	}

}
