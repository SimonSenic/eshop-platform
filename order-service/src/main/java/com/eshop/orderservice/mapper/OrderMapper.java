package com.eshop.orderservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import com.eshop.orderservice.dto.OrderDTO;
import com.eshop.orderservice.entity.Order;

@Mapper(componentModel="spring")
public interface OrderMapper {
	OrderDTO toDTO(Order order);
	
	default Page<OrderDTO> toDTOs(Page<Order> orders){
		return orders.map(order -> toDTO(order));
	}
	
	List<OrderDTO> toDTOs(List<Order> orders);
}
