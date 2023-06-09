package com.eshop.paymentservice.service;

import org.springframework.stereotype.Service;

import com.eshop.paymentservice.dto.PaymentDTO;
import com.eshop.paymentservice.kafka.Producer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {
	private final Producer producer;

	public void pay(Long orderId, PaymentDTO paymentDTO) {
		 producer.sendMessage(orderId);
		 log.info("Payment successful (orderId: {})", orderId);
		 log.info("Send payment confirmation email (orderId: {})", orderId);
	}
}
