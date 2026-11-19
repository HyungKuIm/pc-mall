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

	/** 역할 (ADMIN: 관리자, CUSTOMER: 일반 회원). 값이 없으면 일반 회원으로 취급한다. */
	@Column(name = "role", length = 20)
	private String role;

	protected Customer() {
	}

	/** 회원 가입용. 가입한 회원은 항상 일반 회원(CUSTOMER)이다. */
	public Customer(String name, String encodedPassword, String mail) {
		this.name = name;
		this.password = encodedPassword;
		this.mail = mail;
		this.role = Role.CUSTOMER.name();
	}

	/** 역할. null 이거나 알 수 없는 값이면 CUSTOMER. */
	public Role getRole() {
		return Role.from(role);
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
