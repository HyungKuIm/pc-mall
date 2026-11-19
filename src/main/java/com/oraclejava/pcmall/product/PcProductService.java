package com.oraclejava.pcmall.product;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PcProductService {

	private final PcProductRepository repository;

	public PcProductService(PcProductRepository repository) {
		this.repository = repository;
	}

	public List<PcProductResponse> findAll() {
		return repository.findAllSummaries();
	}

	public PcProductResponse findById(Long id) {
		return PcProductResponse.from(get(id));
	}

	public byte[] findPicS(Long id) {
		return requireImage(get(id).getPicS());
	}

	public byte[] findPicL(Long id) {
		return requireImage(get(id).getPicL());
	}

	@Transactional
	public PcProductResponse create(PcProductForm form) {
		PcProduct product = new PcProduct(form.name(), form.price(), form.kind(), form.text());
		product.setPicS(toBytes(form.picS()));
		product.setPicL(toBytes(form.picL()));
		return PcProductResponse.from(repository.save(product));
	}

	/**
	 * 수정. 이미지 파일을 보내지 않으면 기존 이미지를 유지한다.
	 */
	@Transactional
	public PcProductResponse update(Long id, PcProductForm form) {
		PcProduct product = get(id);
		product.update(form.name(), form.price(), form.kind(), form.text());
		byte[] picS = toBytes(form.picS());
		if (picS != null) {
			product.setPicS(picS);
		}
		byte[] picL = toBytes(form.picL());
		if (picL != null) {
			product.setPicL(picL);
		}
		return PcProductResponse.from(product);
	}

	@Transactional
	public void delete(Long id) {
		repository.delete(get(id));
	}

	private PcProduct get(Long id) {
		return repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다. id=" + id));
	}

	private static byte[] requireImage(byte[] image) {
		if (image == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지가 없습니다.");
		}
		return image;
	}

	private static byte[] toBytes(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		try {
			return file.getBytes();
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
