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
	private final KafkaTemplate<String, String> kafkaTemplate;
	
	@NonNull
	private final ObjectMapper objectMapper;
	
	@Value("${product.topic.name}")
	private String productTopicName;
	
	public void sendMessage(ProductDTO product, Integer amount, String token) throws JsonProcessingException{
		StringBuilder sb = new StringBuilder();
		sb.append(objectMapper.writeValueAsString(product) +"\n");
		sb.append(amount +"\n");
		sb.append(token);
		kafkaTemplate.send(productTopicName, sb.toString());
	}
}
