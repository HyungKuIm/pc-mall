package com.oraclejava.pcmall.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oraclejava.pcmall.customer.CustomerRepository;
import com.oraclejava.pcmall.customer.CustomerService;

/**
 * 메일주소로 회원을 조회해 Spring Security 인증에 사용한다.
 */
@Service
public class CustomerUserDetailsService implements UserDetailsService {

	private final CustomerRepository repository;

	public CustomerUserDetailsService(CustomerRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
		return repository.findByMail(CustomerService.normalizeMail(mail))
			.map(CustomerUserDetails::new)
			.orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
	}

}
