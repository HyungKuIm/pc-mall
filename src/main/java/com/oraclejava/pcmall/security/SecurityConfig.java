package com.oraclejava.pcmall.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import com.oraclejava.pcmall.customer.Role;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Security 설정 (세션 기반 폼 로그인, JSON 응답).
 *
 * <ul>
 * <li>로그인: POST /api/login (form-urlencoded: mail, password) → 200 + 회원 JSON / 실패 시 401</li>
 * <li>로그아웃: POST /api/logout → 204</li>
 * <li>미인증 상태로 보호 자원 접근 시 → 401 (로그인 페이지 리다이렉트 없음)</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JsonMapper jsonMapper) throws Exception {
		http
			// REST API 클라이언트용 단순 구성. 브라우저 화면(쿠키 세션)을 붙일 경우 CSRF 토큰 사용을 검토할 것.
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/health", "/error").permitAll()
				.requestMatchers("/api/customers/signup", "/api/login").permitAll()
				// 상품: 조회는 누구나, 등록/수정/삭제는 관리자만
				.requestMatchers(HttpMethod.GET, "/api/pc-products/**").permitAll()
				.requestMatchers("/api/pc-products/**").hasRole(Role.ADMIN.name())
				.requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())
				.anyRequest().authenticated())
			.formLogin(form -> form
				.loginProcessingUrl("/api/login")
				.usernameParameter("mail")
				.passwordParameter("password")
				.successHandler((request, response, authentication) -> {
					CustomerUserDetails user = (CustomerUserDetails) authentication.getPrincipal();
					writeJson(response, jsonMapper, HttpStatus.OK,
							Map.of("id", user.getId(), "name", user.getName(), "mail", user.getUsername(),
									"role", user.getRole().name()));
				})
				.failureHandler((request, response, exception) -> writeJson(response, jsonMapper,
						HttpStatus.UNAUTHORIZED, Map.of("message", "메일 또는 비밀번호가 올바르지 않습니다."))))
			.logout(logout -> logout
				.logoutUrl("/api/logout")
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
			.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
		return http.build();
	}

	/** 관리자는 일반 회원 권한도 모두 가진다. */
	@Bean
	static RoleHierarchy roleHierarchy() {
		return RoleHierarchyImpl.fromHierarchy(Role.ADMIN.authority() + " > " + Role.CUSTOMER.authority());
	}

	/** {bcrypt} 접두어 방식의 위임 인코더 (기본 BCrypt) */
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	private static void writeJson(HttpServletResponse response, JsonMapper jsonMapper, HttpStatus status,
			Object body) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		jsonMapper.writeValue(response.getOutputStream(), body);
	}

}
