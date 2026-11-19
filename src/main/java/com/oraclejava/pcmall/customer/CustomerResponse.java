package com.oraclejava.pcmall.customer;

/**
 * 회원 응답. 비밀번호는 포함하지 않는다.
 */
public record CustomerResponse(Long id, String name, String mail, Role role) {

	public static CustomerResponse from(Customer c) {
		return new CustomerResponse(c.getId(), c.getName(), c.getMail(), c.getRole());
	}

}
