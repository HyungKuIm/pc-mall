package com.oraclejava.pcmall.order;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByCustomerIdOrderByIdDesc(Long customerId);

	Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

	/**
	 * 주문선 요약. 제품 BLOB 을 읽지 않도록 필요한 컬럼만 가져온다.
	 */
	@Query("""
			select new com.oraclejava.pcmall.order.OrderLineRow(o.id, l.id, p.id, p.name, l.quantity)
			from Order o join o.orderLines l join l.product p
			where o.id in :orderIds
			order by l.id
			""")
	List<OrderLineRow> findLineRows(@Param("orderIds") Collection<Long> orderIds);

}
