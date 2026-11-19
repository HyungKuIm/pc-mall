package com.oraclejava.pcmall.customer;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oraclejava.pcmall.security.CustomerUserDetails;

/**
 * 회원 API.
 * 로그인/로그아웃은 Spring Security 필터가 처리한다 (POST /api/login, POST /api/logout).
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerService service;

	public CustomerController(CustomerService service) {
		this.service = service;
	}

	/** 회원 가입 */
	@PostMapping("/signup")
	public ResponseEntity<CustomerResponse> signup(@Validated @RequestBody SignupRequest request) {
		CustomerResponse created = service.signup(request);
		return ResponseEntity.created(URI.create("/api/customers/me")).body(created);
	}

	/** 로그인한 회원 정보 */
	@GetMapping("/me")
	public CustomerResponse me(@AuthenticationPrincipal CustomerUserDetails user) {
		return service.findByMail(user.getUsername());
	}

}
