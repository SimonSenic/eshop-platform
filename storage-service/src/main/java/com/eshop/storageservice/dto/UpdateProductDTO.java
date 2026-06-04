package com.eshop.storageservice.dto;

import static com.eshop.storageservice.dto.ProductConstants.AVAILABILITY_VALUE;
import static com.eshop.storageservice.dto.ProductConstants.DESCRIPTION_SIZE;
import static com.eshop.storageservice.dto.ProductConstants.NAME_SIZE;
import static com.eshop.storageservice.dto.ProductConstants.PRICE_VALUE;

import java.math.BigDecimal;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProductDTO {
	
	@Size(min = 1, max = 50, message = NAME_SIZE)
	private String name;
	
	@Size(min = 1, max = 3000, message = DESCRIPTION_SIZE)
	private String description;
	
	@Positive(message = PRICE_VALUE)
	private BigDecimal price;
	
	@PositiveOrZero(message = AVAILABILITY_VALUE)
	private Integer availability;
	
}
