package com.eshop.storageservice.dto;

import static com.eshop.storageservice.dto.ProductConstants.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {
	private Long id;
	
	@NotNull(message = NAME_NOT_NULL)
	@Size(min = 1, max = 50, message = NAME_SIZE)
	private String name;
	
	@NotNull(message = PRICE_NOT_NULL)
	@Positive(message = PRICE_VALUE)
	private Double price;
	
	@Positive(message = AVAILABILITY_VALUE)
	private int availability;
}
