package com.eshop.orderservice.kafka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.eshop.orderservice.dto.ProductDTO;
import com.eshop.orderservice.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class Consumer {
	private final OrderService orderService;
	private final ObjectMapper objectMapper;
	
	private final String productTopic = "${product.topic.name}";
	private final String paymentTopic = "${payment.topic.name}";
	
	@KafkaListener(topics = productTopic)
	public void consumeProduct(String message) throws JsonProcessingException, IOException {
		BufferedReader reader = new BufferedReader(new StringReader(message));
		ProductDTO product = objectMapper.readValue(reader.readLine(), ProductDTO.class);
		Integer amount = Integer.valueOf(reader.readLine());
		String token = reader.readLine();
		orderService.createOrder(product, amount, token);
	}
	
	@KafkaListener(topics = paymentTopic)
	public void consumePayment(String message) throws IOException {
		Long orderId = Long.valueOf(message);
		orderService.completePayment(orderId);
	}
}
