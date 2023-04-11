package com.eshop.orderservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.eshop.orderservice.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long>{
	Optional<Item> findByOrderIdAndProductId(Long orderId, Long productId);
	
	@Modifying
    @Query("DELETE FROM Item i WHERE i.order.id = :orderId")
	void deleteByOrderId(Long orderId);
}
