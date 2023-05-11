package com.eshop.paymentservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Producer {
	@NonNull
	private final KafkaTemplate<String, String> kafkaTemplate;
	
	@Value("${payment.topic.name}")
	private String paymentTopicName;
	
	public void sendMessage(Long orderId){
		kafkaTemplate.send(paymentTopicName, orderId.toString());
	}

}
