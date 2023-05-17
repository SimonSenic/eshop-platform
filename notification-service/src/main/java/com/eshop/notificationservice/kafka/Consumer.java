package com.eshop.notificationservice.kafka;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.eshop.notificationservice.exception.BusinessException;
import com.eshop.notificationservice.service.NotificationService;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class Consumer {
	private final NotificationService notificationService;
	
	private final String logTopicName = "${log.topic.name}";
	private final String customerRegistrationTopicName = "${customer-registration.topic.name}";
	private final String adminCreationTopicName = "${admin-creation.topic.name}";
	private final String paymentConfirmationTopicName = "${payment-confirmation.topic.name}";
	private final String orderProcessingTopicName = "${order-processing.topic.name}";
	private final String orderCancellationTopicName = "${order-cancellation.topic.name}";
	
	@KafkaListener(topics = logTopicName)
	public void consumeLog(String message) {
		notificationService.log(message);
	}
	
	@KafkaListener(topics = { customerRegistrationTopicName, adminCreationTopicName })
	public void consumeUserEmail(ConsumerRecord<String, String> record) throws MessagingException, BusinessException {
		Pattern pattern = Pattern.compile("\\(userId: (\\d+)\\)");
        Matcher matcher = pattern.matcher(record.value());
        
        if(matcher.find()) {
        	Long userId = Long.valueOf(matcher.group(1));
        	switch(record.topic()) {
        		case "customer-registration": {
        			System.out.println("NOO?");
        			notificationService.sendConfirmRegistratonEmail(userId);
        		}
        		case "admin-creation": {
        			notificationService.sendCompleteRegistratonEmail(userId);
        		}
        	}	
        	
        }else {
        	throw new BusinessException("Invalid userId");
        }
	}
	
	@KafkaListener(topics = { paymentConfirmationTopicName, orderProcessingTopicName, orderCancellationTopicName })
	public void consumeOrderEmail(ConsumerRecord<String, String> record) throws MessagingException, BusinessException {
		Pattern pattern = Pattern.compile("\\(userId: (\\d+)\\)");
        Matcher matcher = pattern.matcher(record.value());
        
        if(matcher.find()) {
        	Long orderId = Long.valueOf(matcher.group(1));
        	switch(record.topic()) {
        		case "payment-confirmation": {
        			notificationService.sendPaymentConfirmationEmail(orderId);
        		}
        		case "order-processing": {
        			notificationService.sendOrderProcessingEmail(orderId);
        		}
        		case "order-cancellation": {
        			notificationService.sendOrderCancellationEmail(orderId);
        		}
        	}	
        	
        }else {
        	throw new BusinessException("Invalid orderId");
        }
	}
	
}
