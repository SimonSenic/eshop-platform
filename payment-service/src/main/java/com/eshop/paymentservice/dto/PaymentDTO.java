package com.eshop.paymentservice.dto;

import static com.eshop.paymentservice.dto.PaymentConstants.*;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDTO {
	
	@NotNull(message = PAYMENT_METHOD_NOT_NULL)
	@Size(min = 3, max = 30, message = PAYMENT_METHOD_SIZE)
	private String paymentMethod;
	
	@NotNull(message = TOTAL_AMOUNT_NOT_NULL)
	@Positive(message = TOTAL_AMOUNT_VALUE)
	private BigDecimal totalAmount;
	
	@NotNull(message = CURRENCY_NOT_NULL)
	@Size(min = 3, max = 3, message = CURRENCY_SIZE)
	private String currency;
}
