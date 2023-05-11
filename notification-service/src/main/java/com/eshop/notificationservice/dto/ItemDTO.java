package com.eshop.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDTO {
	private Long id;
	
	private Long productId;
	
	private Integer amount;
	
	private Double price;
}
