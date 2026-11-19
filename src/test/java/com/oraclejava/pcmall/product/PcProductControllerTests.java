package com.oraclejava.pcmall.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PcProductControllerTests {

	// PNG 시그니처로 시작하는 더미 이미지
	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 1, 2, 3 };

	@Autowired
	private MockMvc mockMvc;

	@Test
	void crudFlow() throws Exception {
		// 등록
		MvcResult created = mockMvc.perform(multipart("/api/pc-products")
				.file(new MockMultipartFile("picS", "s.png", "image/png", PNG))
				.file(new MockMultipartFile("picL", "l.png", "image/png", PNG))
				.param("name", "ThinkPad X1 Carbon")
				.param("price", "650000")
				.param("kind", "비즈니스")
				.param("text", "배터리 상태 양호"))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andExpect(jsonPath("$.name").value("ThinkPad X1 Carbon"))
			.andExpect(jsonPath("$.picSUrl").exists())
			.andReturn();
		Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		// 단건 / 목록 조회
		mockMvc.perform(get("/api/pc-products/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.price").value(650000))
			.andExpect(jsonPath("$.picLUrl").value("/api/pc-products/" + id + "/pic-l"));
		mockMvc.perform(get("/api/pc-products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id == " + id + ")].kind").value("비즈니스"));

		// 이미지 조회
		mockMvc.perform(get("/api/pc-products/{id}/pic-s", id))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.IMAGE_PNG))
			.andExpect(content().bytes(PNG));

		// 수정 (이미지 미전송 → 기존 이미지 유지)
		mockMvc.perform(multipart(HttpMethod.PUT, "/api/pc-products/{id}", id)
				.param("name", "ThinkPad X1 Carbon Gen9")
				.param("price", "600000"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("ThinkPad X1 Carbon Gen9"))
			.andExpect(jsonPath("$.picSUrl").exists());

		// 삭제
		mockMvc.perform(delete("/api/pc-products/{id}", id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/pc-products/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void createWithInvalidInputReturnsBadRequest() throws Exception {
		mockMvc.perform(multipart("/api/pc-products").param("price", "-1"))
			.andExpect(status().isBadRequest());
	}

}
