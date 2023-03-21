package com.eshop.orderservice.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.orderservice.dto.OrderDTO;
import com.eshop.orderservice.service.OrderService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/order-service/orders")
@AllArgsConstructor
public class OrderController {
	private OrderService orderService;
	
	@GetMapping
	public ResponseEntity<Page<OrderDTO>> getOrders(Pageable pageable){
		return ResponseEntity.ok(orderService.getOrders(pageable));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<OrderDTO> getOrder(@PathVariable(value = "id") Long id){
		return ResponseEntity.ok(orderService.getOrder(id));
	}
	
	@GetMapping("/my")
	public ResponseEntity<List<OrderDTO>> getUserOrders(){
		return ResponseEntity.ok(orderService.getUserOrders());
	}
	
	@PatchMapping("/cancel/{id}")
	public ResponseEntity<OrderDTO> cancelOrder(@PathVariable(value = "id") Long id){
		return ResponseEntity.ok(orderService.cancelOrder(id)); 
	}
	
	@PatchMapping("/confirm/{id}")
	public ResponseEntity<OrderDTO> confirmOrder(@PathVariable(value = "id") Long id){
		return ResponseEntity.ok(orderService.confirmOrder(id));
	}
	
	@PatchMapping("/complete/{id}")
	public ResponseEntity<OrderDTO> completeOrder(@PathVariable(value = "id") Long id){
		return ResponseEntity.ok(orderService.completeOrder(id));
	}
}
