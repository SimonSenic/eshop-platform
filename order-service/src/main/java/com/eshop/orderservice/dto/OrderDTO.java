package com.eshop.orderservice.dto;

import java.util.List;

import com.eshop.orderservice.entity.State;

import lombok.Data;

@Data
public class OrderDTO {
	private Long id;
	
	private Long userId;
	
	private List<ItemDTO> cart;
	
	private Double price;
	
	private State state;
}
