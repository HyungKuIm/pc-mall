package com.oraclejava.pcmall.health;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 헬스체크 엔드포인트.
 * 로드밸런서 / 모니터링 도구가 애플리케이션 생존 여부를 확인할 때 사용한다.
 */
@RestController
public class HealthController {

	private final String applicationName;

	public HealthController(@Value("${spring.application.name}") String applicationName) {
		this.applicationName = applicationName;
	}

	@GetMapping("/health")
	public Map<String, Object> health() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", "UP");
		body.put("application", applicationName);
		body.put("timestamp", Instant.now().toString());
		return body;
	}

}
