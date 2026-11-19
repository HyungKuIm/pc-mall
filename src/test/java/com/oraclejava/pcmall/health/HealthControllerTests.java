package com.oraclejava.pcmall.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.oraclejava.pcmall.security.SecurityConfig;

@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthReturnsUp() throws Exception {
		mockMvc.perform(get("/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"))
			.andExpect(jsonPath("$.application").value("pc-mall"))
			.andExpect(jsonPath("$.timestamp").exists());
	}

}
