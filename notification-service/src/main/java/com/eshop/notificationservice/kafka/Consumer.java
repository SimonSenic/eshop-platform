package com.eshop.notificationservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.eshop.notificationservice.service.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class Consumer {
	private final NotificationService notificationService;
	
	private final String logTopic = "${log.topic.name}";
	
	@KafkaListener(topics = logTopic)
	public void consumeLog(String message) {
		log.info(message);
		notificationService.sendEmail("simon.senic@kosickaakademia.sk", "test", "This is a test message");
	}
}
