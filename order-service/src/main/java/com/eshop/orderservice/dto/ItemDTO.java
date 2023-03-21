package com.eshop.orderservice.dto;

import lombok.Data;

@Data
public class ItemDTO {
	private Long id;
	
	private Long productId;
	
	private Long amount;
	
	private Double price;
}
