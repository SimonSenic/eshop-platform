package com.eshop.orderservice.kafka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.eshop.orderservice.dto.OrderDTO;
import com.eshop.orderservice.dto.ProductDTO;
import com.eshop.orderservice.entity.Order;
import com.eshop.orderservice.entity.State;
import com.eshop.orderservice.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class Consumer {
	private OrderService orderService;
	private ObjectMapper objectMapper;
	
	private final String orderTopic = "${order.topic.name}";
	//private final String paymentTopic = "${payment.topic.name}";
	
	@KafkaListener(topics = orderTopic)
	public void consumeOrder(String message) throws JsonProcessingException, IOException {
		BufferedReader reader = new BufferedReader(new StringReader(message));
		ProductDTO product = objectMapper.readValue(reader.readLine(), ProductDTO.class);
		Long userId = Long.valueOf(reader.readLine());
		Integer amount = Integer.valueOf(reader.readLine());
		
		System.out.println(orderService.getUserOrders().stream().map(order -> order.getState().equals(State.DRAFT)).findAny().isEmpty());
		
		//Order order = new Order(userId, product.getName(), amount, product.getPrice()*amount, State.DRAFT);
		//orderService.save(order);
	}
}
