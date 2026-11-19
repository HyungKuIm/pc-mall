package com.oraclejava.pcmall.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerAuthTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CustomerRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private void signup(String name, String mail, String password) throws Exception {
		mockMvc.perform(post("/api/customers/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"%s","mail":"%s","password":"%s"}
						""".formatted(name, mail, password)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.mail").value(mail.toLowerCase()))
			.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void signupLoginMeLogoutFlow() throws Exception {
		signup("홍길동", "Hong@Example.com", "password123");

		// 비밀번호는 인코딩되어 저장된다
		Customer saved = repository.findByMail("hong@example.com").orElseThrow();
		assertThat(saved.getPassword()).startsWith("{bcrypt}").doesNotContain("password123");

		// 로그인 전에는 내 정보 조회 불가
		mockMvc.perform(get("/api/customers/me")).andExpect(status().isUnauthorized());

		// 메일주소를 사용자이름으로 로그인 (대소문자 무시)
		MvcResult login = mockMvc.perform(post("/api/login")
				.param("mail", "HONG@example.com")
				.param("password", "password123"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.mail").value("hong@example.com"))
			.andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

		mockMvc.perform(get("/api/customers/me").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(saved.getId()))
			.andExpect(jsonPath("$.name").value("홍길동"));

		// 로그아웃 후 세션 무효화
		mockMvc.perform(post("/api/logout").session(session)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/customers/me").session(session)).andExpect(status().isUnauthorized());
	}

	@Test
	void signupAlwaysCreatesCustomerRole() throws Exception {
		signup("박민수", "park@example.com", "password123");
		assertThat(repository.findByMail("park@example.com").orElseThrow().getRole()).isEqualTo(Role.CUSTOMER);
	}

	@Test
	void onlyAdminCanManageProducts() throws Exception {
		signup("일반회원", "user@example.com", "password123");
		signup("관리자", "admin@example.com", "password123");
		// 관리자 role 은 DB에 미리 들어가 있는 상황을 재현
		jdbcTemplate.update("update customer set role = 'ADMIN' where mail = ?", "admin@example.com");

		MockHttpSession admin = login("admin@example.com", "password123", "ADMIN");
		MockHttpSession user = login("user@example.com", "password123", "CUSTOMER");

		// 상품 조회는 누구나
		mockMvc.perform(get("/api/pc-products")).andExpect(status().isOk());

		// 상품 등록: 비로그인 401, 일반회원 403, 관리자 201
		mockMvc.perform(multipart("/api/pc-products").param("name", "노트북").param("price", "1000"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(multipart("/api/pc-products").param("name", "노트북").param("price", "1000").session(user))
			.andExpect(status().isForbidden());
		mockMvc.perform(multipart("/api/pc-products").param("name", "노트북").param("price", "1000").session(admin))
			.andExpect(status().isCreated());

		// 관리자도 회원 기능(내 정보)을 사용할 수 있다
		mockMvc.perform(get("/api/customers/me").session(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	private MockHttpSession login(String mail, String password, String expectedRole) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/login").param("mail", mail).param("password", password))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.role").value(expectedRole))
			.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	@Test
	void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
		signup("김철수", "kim@example.com", "password123");
		mockMvc.perform(post("/api/login").param("mail", "kim@example.com").param("password", "wrong-pass"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").exists());
		mockMvc.perform(post("/api/login").param("mail", "nobody@example.com").param("password", "password123"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void duplicateMailReturnsConflict() throws Exception {
		signup("이영희", "lee@example.com", "password123");
		mockMvc.perform(post("/api/customers/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"다른사람","mail":"LEE@example.com","password":"password456"}
						"""))
			.andExpect(status().isConflict());
	}

	@Test
	void invalidSignupReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/customers/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"","mail":"not-an-email","password":"short"}
						"""))
			.andExpect(status().isBadRequest());
	}

}
