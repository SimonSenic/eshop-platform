package com.eshop.storageservice.dto;

import org.hibernate.validator.constraints.Range;

import static com.eshop.storageservice.dto.ProductConstants.*;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductDTO {
	private Long id;
	
	@Size(min = 1, max = 50, message = NAME_SIZE)
	private String name;
	
	@Range(min = 0, message = NUM_MIN_SIZE)
	private Double price;
	
	@Range(min = 0, message = NUM_MIN_SIZE)
	private int availability;
}
