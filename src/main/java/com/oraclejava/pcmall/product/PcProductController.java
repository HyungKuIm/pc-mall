package com.oraclejava.pcmall.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 중고 노트북 상품(pc_product) CRUD API.
 */
@RestController
@RequestMapping("/api/pc-products")
public class PcProductController {

	private final PcProductService service;

	public PcProductController(PcProductService service) {
		this.service = service;
	}

	/** 목록 조회 */
	@GetMapping
	public List<PcProductResponse> list() {
		return service.findAll();
	}

	/** 단건 조회 */
	@GetMapping("/{id}")
	public PcProductResponse get(@PathVariable Long id) {
		return service.findById(id);
	}

	/** 등록 (multipart/form-data: name, price, kind, text, picS, picL) */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<PcProductResponse> create(@Validated @ModelAttribute PcProductForm form) {
		PcProductResponse created = service.create(form);
		return ResponseEntity.created(URI.create("/api/pc-products/" + created.id())).body(created);
	}

	/** 수정 (이미지 미전송 시 기존 이미지 유지) */
	@PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public PcProductResponse update(@PathVariable Long id, @Validated @ModelAttribute PcProductForm form) {
		return service.update(id, form);
	}

	/** 삭제 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	/** 작은 이미지 조회 */
	@GetMapping("/{id}/pic-s")
	public ResponseEntity<byte[]> picS(@PathVariable Long id) {
		return image(service.findPicS(id));
	}

	/** 큰 이미지 조회 */
	@GetMapping("/{id}/pic-l")
	public ResponseEntity<byte[]> picL(@PathVariable Long id) {
		return image(service.findPicL(id));
	}

	private static ResponseEntity<byte[]> image(byte[] bytes) {
		return ResponseEntity.ok().contentType(detectType(bytes)).body(bytes);
	}

	/** 바이트 시그니처로 이미지 타입 추정 (실패 시 octet-stream) */
	private static MediaType detectType(byte[] bytes) {
		try {
			String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
			return (type != null) ? MediaType.parseMediaType(type) : MediaType.APPLICATION_OCTET_STREAM;
		}
		catch (IOException ex) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

}
