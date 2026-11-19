package com.oraclejava.pcmall.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.oraclejava.pcmall.customer.Customer;
import com.oraclejava.pcmall.customer.Role;

/**
 * 인증된 회원 정보. 사용자이름(username)은 메일주소이다.
 */
public class CustomerUserDetails implements UserDetails {

	private final Long id;

	private final String name;

	private final String mail;

	private final String password;

	private final Role role;

	public CustomerUserDetails(Customer customer) {
		this.id = customer.getId();
		this.name = customer.getName();
		this.mail = customer.getMail();
		this.password = customer.getPassword();
		this.role = customer.getRole();
	}

	public Long getId() {
		return id;
	}

	public Role getRole() {
		return role;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getUsername() {
		return mail;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.authority()));
	}

}
