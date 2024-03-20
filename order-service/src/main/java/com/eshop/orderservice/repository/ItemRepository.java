package com.eshop.orderservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eshop.orderservice.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long>{
	Optional<Item> findByOrderIdAndProductId(Long orderId, Long productId);
}
