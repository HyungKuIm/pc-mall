package com.oraclejava.pcmall.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PcProductRepository extends JpaRepository<PcProduct, Long> {

	/**
	 * 목록 조회용. BLOB 본문은 읽지 않고, 이미지 존재 여부만 가져온다.
	 */
	@Query("""
			select new com.oraclejava.pcmall.product.PcProductResponse(
				p.id, p.name, p.price, p.kind, p.text,
				case when p.picS is not null then true else false end,
				case when p.picL is not null then true else false end)
			from PcProduct p
			order by p.id desc
			""")
	List<PcProductResponse> findAllSummaries();

}
