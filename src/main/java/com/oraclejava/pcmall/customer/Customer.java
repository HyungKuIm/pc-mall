package com.oraclejava.pcmall.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 회원 (customer 테이블). 로그인 사용자이름으로 mail 을 사용한다.
 */
@Entity
@Table(name = "customer")
public class Customer {

	/** 회원ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	/** 회원명 */
	@Column(name = "name", nullable = false, length = 100)
	private String name;

	/** 로그인 비밀번호 (인코딩된 값만 저장) */
	@Column(name = "password", nullable = false, length = 200)
	private String password;

	/** 회원 email (로그인 사용자이름, 유일) */
	@Column(name = "mail", nullable = false, unique = true, length = 255)
	private String mail;

	protected Customer() {
	}

	public Customer(String name, String encodedPassword, String mail) {
		this.name = name;
		this.password = encodedPassword;
		this.mail = mail;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getMail() {
		return mail;
	}

}
