package com.eshop.paymentservice.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eshop.paymentservice.dto.PaymentDTO;
import com.eshop.paymentservice.service.PaymentService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/payment-service/payment")
@AllArgsConstructor
public class PaymentController {
	private final PaymentService paymentService;

	@PostMapping("/{orderId}")
	public void pay(@PathVariable Long orderId, @RequestBody @Valid PaymentDTO productDTO){
		paymentService.pay(orderId, productDTO);
	}
}
