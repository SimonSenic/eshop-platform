package com.eshop.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

import com.eshop.orderservice.entity.State;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDTO {
	private Long id;
	
	private Long userId;
	
	private List<ItemDTO> cart;
	
	private BigDecimal totalPrice;
	
	private State state;
}
