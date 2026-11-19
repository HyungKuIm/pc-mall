package com.oraclejava.pcmall.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	Optional<Customer> findByMail(String mail);

	boolean existsByMail(String mail);

}
