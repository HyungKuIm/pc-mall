package com.oraclejava.pcmall.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원 가입 요청 (application/json).
 * 비밀번호 최대 72자는 BCrypt 입력 길이 제한에 맞춘 값이다.
 */
public record SignupRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Email @Size(max = 255) String mail,
		@NotBlank @Size(min = 4, max = 72) String password) {
}
