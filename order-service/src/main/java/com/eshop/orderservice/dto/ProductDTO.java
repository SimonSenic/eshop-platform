package com.eshop.orderservice.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {
	private Long id;
	
	private String name;
	
	private BigDecimal price;
	
	private int availability;
}
