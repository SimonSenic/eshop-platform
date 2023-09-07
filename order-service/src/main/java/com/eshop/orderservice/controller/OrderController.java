package com.eshop.orderservice.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.orderservice.dto.OrderDTO;
import com.eshop.orderservice.entity.State;
import com.eshop.orderservice.exception.BusinessException;
import com.eshop.orderservice.exception.NotFoundException;
import com.eshop.orderservice.service.OrderService;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/order-service/orders")
@AllArgsConstructor
public class OrderController {
	private final OrderService orderService;
	
	@GetMapping
	public ResponseEntity<Page<OrderDTO>> getOrders(Pageable pageable){
		return ResponseEntity.ok(orderService.getOrders(pageable));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id){
		return ResponseEntity.ok(orderService.getOrder(id));
	}
	
	@GetMapping("/my")
	@Retry(name = "orderService", fallbackMethod = "userServiceDownFallback")
	public ResponseEntity<List<OrderDTO>> getMyOrders(){
		return ResponseEntity.ok(orderService.getMyOrders());
	}
	
	@PatchMapping("/{id}/update")
	@Retry(name = "orderService", fallbackMethod = "storageServiceDownFallback")
	public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, 
			@RequestParam(required = false) Long productId, @RequestParam(required = false, defaultValue = "0") Integer amount){
		return ResponseEntity.ok(orderService.updateOrder(id, productId, amount));
	}
	
	@PatchMapping("/{id}/cancel")
	@Retry(name = "orderService", fallbackMethod = "storageServiceDownFallback")
	public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id){
		return ResponseEntity.ok(orderService.cancelOrder(id)); 
	}
	
	@PatchMapping("/{id}/process")
	public ResponseEntity<OrderDTO> processOrder(@PathVariable Long id, @RequestParam State state){
		return ResponseEntity.ok(orderService.processOrder(id, state));
	}
	
	public ResponseEntity<OrderDTO> userServiceDownFallback(Exception exception) throws Exception {
		if(!(exception instanceof BusinessException)) {
			throw new BusinessException("User service is down");
		}else {
			throw exception;
		}
    }
	
	public ResponseEntity<OrderDTO> storageServiceDownFallback(Exception exception) throws Exception {
		if(!(exception instanceof BusinessException) && !(exception instanceof NotFoundException)) {
			throw new BusinessException("Storage service is down");
		}else {
			throw exception;
		}
    }
	
}
