package com.eshop.orderservice.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.eshop.orderservice.dto.OrderDTO;
import com.eshop.orderservice.entity.Order;
import com.eshop.orderservice.entity.State;
import com.eshop.orderservice.exception.BusinessException;
import com.eshop.orderservice.exception.NotFoundException;
import com.eshop.orderservice.mapper.OrderMapper;
import com.eshop.orderservice.repository.OrderRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;
	private final ApiClient apiClient;
	
	public Page<OrderDTO> getOrders(Pageable pageable){
		return orderMapper.toDTOs(orderRepository.findAll(pageable));
	}

	public OrderDTO getOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		return orderMapper.toDTO(order);
	}
	
	public List<OrderDTO> getUserOrders(){
		List<Order> orders = orderRepository.findByUserId(apiClient.getUserProfile().getId());
		return orderMapper.toDTOs(orders);
	}
	
	public OrderDTO cancelOrder(Long id) { // availability refund ?
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		//if(order.getState().equals())
		
		order.setState(State.CANCELLED);
		orderRepository.save(order);
		return orderMapper.toDTO(order);
	}
	
	public OrderDTO confirmOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		if(!order.getState().equals(State.ORDER)) {
			throw new BusinessException("Invalid order state");
		}
		
		order.setState(State.CONFIRMED);
		orderRepository.save(order);
		return orderMapper.toDTO(order);
	}
	
	public OrderDTO completeOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		if(!order.getState().equals(State.CONFIRMED)) {
			throw new BusinessException("Invalid order state");
		}
		
		order.setState(State.COMPLETED);
		orderRepository.save(order);
		return orderMapper.toDTO(order);
	}
}
