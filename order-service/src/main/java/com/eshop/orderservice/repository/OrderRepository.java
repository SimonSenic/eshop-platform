package com.eshop.orderservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eshop.orderservice.entity.Order;
import com.eshop.orderservice.entity.State;

public interface OrderRepository extends JpaRepository<Order, Long>{
	List<Order> findByUserId(Long userId);
	
	Optional<Order> findByUserIdAndState(Long userId, State state);
}
