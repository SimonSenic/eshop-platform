package com.eshop.storageservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.eshop.storageservice.dto.ProductDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Producer {
	@NonNull
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@NonNull
	private ObjectMapper objectMapper;
	
	@Value("${order.topic.name}")
	private String orderTopic;
	
	public void sendMessage(ProductDTO product, String username, Integer amount) throws JsonProcessingException{
		StringBuilder sb = new StringBuilder();
		sb.append(objectMapper.writeValueAsString(product) +"\n");
		sb.append(username +"\n");
		sb.append(amount);
		kafkaTemplate.send(orderTopic, sb.toString());
	}
}
