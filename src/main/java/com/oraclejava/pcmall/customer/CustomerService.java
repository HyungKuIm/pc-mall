package com.oraclejava.pcmall.customer;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CustomerService {

	private final CustomerRepository repository;

	private final PasswordEncoder passwordEncoder;

	public CustomerService(CustomerRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public CustomerResponse signup(SignupRequest request) {
		String mail = normalizeMail(request.mail());
		if (repository.existsByMail(mail)) {
			throw duplicateMail();
		}
		Customer customer = new Customer(request.name().trim(), passwordEncoder.encode(request.password()), mail);
		try {
			return CustomerResponse.from(repository.saveAndFlush(customer));
		}
		catch (DataIntegrityViolationException ex) {
			// 동시 가입으로 unique 제약에 걸린 경우
			throw duplicateMail();
		}
	}

	public CustomerResponse findByMail(String mail) {
		return repository.findByMail(normalizeMail(mail))
			.map(CustomerResponse::from)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
	}

	/** 메일은 대소문자 구분 없이 하나의 계정으로 취급한다. */
	public static String normalizeMail(String mail) {
		return (mail == null) ? null : mail.trim().toLowerCase(Locale.ROOT);
	}

	private static ResponseStatusException duplicateMail() {
		return new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 메일입니다.");
	}

}
