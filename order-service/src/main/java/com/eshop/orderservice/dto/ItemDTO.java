package com.eshop.orderservice.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDTO {
	private Long id;
	
	private Long productId;
	
	private Integer amount;
	
	private BigDecimal price;
}
