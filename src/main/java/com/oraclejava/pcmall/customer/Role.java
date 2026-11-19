package com.oraclejava.pcmall.customer;

import java.util.Locale;

/**
 * 회원 역할. customer.role 컬럼에 이름(문자열)으로 저장된다.
 * Spring Security 권한으로는 "ROLE_" 접두어를 붙여 사용한다 (예: ROLE_ADMIN).
 */
public enum Role {

	CUSTOMER, ADMIN;

	public String authority() {
		return "ROLE_" + name();
	}

	/** DB 값 → Role. 비어 있거나 알 수 없는 값은 CUSTOMER 로 취급한다. */
	public static Role from(String value) {
		if (value != null) {
			String v = value.trim().toUpperCase(Locale.ROOT);
			for (Role role : values()) {
				if (role.name().equals(v)) {
					return role;
				}
			}
		}
		return CUSTOMER;
	}

}
