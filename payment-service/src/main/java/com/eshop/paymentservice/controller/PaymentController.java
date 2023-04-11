package com.eshop.paymentservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.paymentservice.dto.PaymentDTO;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/payment-service/payment")
@AllArgsConstructor
public class PaymentController {

	@PostMapping
	public PaymentDTO addProduct(PaymentDTO productDTO){
		
		return null;
	}
}
