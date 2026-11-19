package com.oraclejava.pcmall.product;

/**
 * 상품 응답. 이미지 바이너리는 포함하지 않고, 이미지 조회 URL을 제공한다.
 */
public record PcProductResponse(
		Long id,
		String name,
		Integer price,
		String kind,
		String text,
		String picSUrl,
		String picLUrl) {

	public PcProductResponse(Long id, String name, Integer price, String kind, String text,
			boolean hasPicS, boolean hasPicL) {
		this(id, name, price, kind, text,
				hasPicS ? imageUrl(id, "pic-s") : null,
				hasPicL ? imageUrl(id, "pic-l") : null);
	}

	public static PcProductResponse from(PcProduct p) {
		return new PcProductResponse(p.getId(), p.getName(), p.getPrice(), p.getKind(), p.getText(),
				p.getPicS() != null, p.getPicL() != null);
	}

	private static String imageUrl(Long id, String type) {
		return "/api/pc-products/" + id + "/" + type;
	}

}
