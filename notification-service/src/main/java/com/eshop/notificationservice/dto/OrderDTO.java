package com.eshop.notificationservice.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDTO {
	private Long id;
	
	private Long userId;
	
	private List<ItemDTO> cart;
	
	private Double totalPrice;
	
	private State state;
}
