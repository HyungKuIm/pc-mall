package com.oraclejava.pcmall.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.oraclejava.pcmall.product.PcProduct;
import com.oraclejava.pcmall.product.PcProductRepository;

@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowTests {

	private static final String ORDER_FORM = """
			{"formName":"홍길동","formMail":"recv@example.com","formMsg":"서울시 중구 1-1, 010-0000-0000"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PcProductRepository productRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Test
	void addToCartThenOrder() throws Exception {
		Long p1 = productRepository.save(new PcProduct("노트북A", 300000, "laptop", null)).getId();
		Long p2 = productRepository.save(new PcProduct("노트북B", 500000, "laptop", null)).getId();
		MockHttpSession session = signupAndLogin();

		// 담기 (같은 상품은 수량 합산)
		addToCart(session, p1, 1).andExpect(status().isOk());
		addToCart(session, p1, 1).andExpect(status().isOk());
		addToCart(session, p2, 3).andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(2))
			.andExpect(jsonPath("$.items[0].quantity").value(2))
			.andExpect(jsonPath("$.totalPrice").value(2 * 300000 + 3 * 500000));

		// 수량 변경
		mockMvc.perform(put("/api/cart/items/" + p2).session(session)
				.contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalPrice").value(2 * 300000 + 500000));

		// 주문 → 확인 메시지 + 주문 내용
		MvcResult result = mockMvc.perform(post("/api/orders").session(session)
				.contentType(MediaType.APPLICATION_JSON).content(ORDER_FORM))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.message").value(OrderCompleteResponse.MESSAGE))
			.andExpect(jsonPath("$.order.totalPrice").value(1100000))
			.andExpect(jsonPath("$.order.formName").value("홍길동"))
			.andExpect(jsonPath("$.order.orderDate").exists())
			.andExpect(jsonPath("$.order.orderLines.length()").value(2))
			.andExpect(jsonPath("$.order.orderLines[0].productName").value("노트북A"))
			.andExpect(jsonPath("$.order.orderLines[0].quantity").value(2))
			.andReturn();
		Long orderId = ((Number) com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(),
				"$.order.id")).longValue();
		assertThat(orderRepository.findById(orderId)).isPresent();

		// 주문 후 장바구니는 비워진다
		mockMvc.perform(get("/api/cart").session(session))
			.andExpect(jsonPath("$.items.length()").value(0))
			.andExpect(jsonPath("$.totalPrice").value(0));

		// 내 주문 조회
		mockMvc.perform(get("/api/orders").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].orderLines.length()").value(2));
		mockMvc.perform(get("/api/orders/" + orderId).session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.formMsg").value("서울시 중구 1-1, 010-0000-0000"));

		// 다른 회원은 남의 주문을 볼 수 없다
		MockHttpSession other = signupAndLogin();
		mockMvc.perform(get("/api/orders/" + orderId).session(other)).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/orders").session(other)).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void emptyCartCannotBeOrdered() throws Exception {
		MockHttpSession session = signupAndLogin();
		mockMvc.perform(post("/api/orders").session(session)
				.contentType(MediaType.APPLICATION_JSON).content(ORDER_FORM))
			.andExpect(status().isBadRequest());
	}

	@Test
	void invalidOrderFormKeepsCart() throws Exception {
		Long p = productRepository.save(new PcProduct("노트북C", 100000, null, null)).getId();
		MockHttpSession session = signupAndLogin();
		addToCart(session, p, 1).andExpect(status().isOk());

		mockMvc.perform(post("/api/orders").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"formName\":\"\",\"formMail\":\"not-mail\",\"formMsg\":\"\"}"))
			.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/cart").session(session)).andExpect(jsonPath("$.items.length()").value(1));
	}

	@Test
	void cartValidation() throws Exception {
		Long p = productRepository.save(new PcProduct("노트북D", 100000, null, null)).getId();
		MockHttpSession session = signupAndLogin();

		addToCart(session, 999999L, 1).andExpect(status().isNotFound());
		addToCart(session, p, 0).andExpect(status().isBadRequest());
		addToCart(session, p, 99).andExpect(status().isOk());
		addToCart(session, p, 1).andExpect(status().isBadRequest());

		mockMvc.perform(delete("/api/cart/items/" + p).session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(0));
		mockMvc.perform(delete("/api/cart/items/" + p).session(session)).andExpect(status().isNotFound());
	}

	@Test
	void cartAndOrdersRequireLogin() throws Exception {
		mockMvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(ORDER_FORM))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void orderedProductCannotBeDeleted() throws Exception {
		Long p = productRepository.save(new PcProduct("노트북E", 100000, null, null)).getId();
		MockHttpSession session = signupAndLogin();
		addToCart(session, p, 1).andExpect(status().isOk());
		mockMvc.perform(post("/api/orders").session(session)
				.contentType(MediaType.APPLICATION_JSON).content(ORDER_FORM))
			.andExpect(status().isCreated());

		mockMvc.perform(delete("/api/pc-products/" + p)
				.with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
					.user("admin").roles("ADMIN")))
			.andExpect(status().isConflict());
		assertThat(productRepository.existsById(p)).isTrue();
	}

	private org.springframework.test.web.servlet.ResultActions addToCart(MockHttpSession session, Long productId,
			int quantity) throws Exception {
		return mockMvc.perform(post("/api/cart/items").session(session)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"productId\":%d,\"quantity\":%d}".formatted(productId, quantity)));
	}

	private MockHttpSession signupAndLogin() throws Exception {
		String mail = "u" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
		mockMvc.perform(post("/api/customers/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"회원\",\"mail\":\"%s\",\"password\":\"password123\"}".formatted(mail)))
			.andExpect(status().isCreated());
		MvcResult login = mockMvc.perform(post("/api/login").param("mail", mail).param("password", "password123"))
			.andExpect(status().isOk())
			.andReturn();
		return (MockHttpSession) login.getRequest().getSession(false);
	}

}
