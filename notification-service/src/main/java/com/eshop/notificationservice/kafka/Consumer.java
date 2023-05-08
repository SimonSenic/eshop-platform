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
	private final String customerRegistrationTopic = "${customer-registration.topic.name}";
	private final String adminCreationTopic = "${admin-creation.topic.name}";
	private final String paymentConfirmationTopic = "${payment-confirmation.topic.name}";
	private final String orderProcessingTopic = "${order-processing.topic.name}";
	private final String orderCancellationTopic = "${order-cancellation.topic.name}";
	
	@KafkaListener(topics = logTopic)
	public void consumeLog(String message) {
		log.info(message);
	}
	
	@KafkaListener(topics = customerRegistrationTopic)
	public void consumeCustomerRegistration(String message) {
		notificationService.sendEmail("simon.senic@kosickaakademia.sk", "Customer email", "This is a test message");
		log.info("Confirm registration email sent");
	}
	
	@KafkaListener(topics = adminCreationTopic)
	public void consumeAdminCreation(String message) {
		notificationService.sendEmail("simon.senic@kosickaakademia.sk", "Admin complete registration email", "This is a test message");
		log.info("Complete registration email sent");
	}
	
	@KafkaListener(topics = paymentConfirmationTopic)
	public void consumePaymentConfirmation(String message) {
		notificationService.sendEmail("simon.senic@kosickaakademia.sk", "Order created!!", "This is a test message");
		log.info("Payment confirmation email sent");
	}
	
	@KafkaListener(topics = orderProcessingTopic)
	public void consumeOrderProcessing(String message) {
		notificationService.sendEmail("simon.senic@kosickaakademia.sk", "Order CONFIRMED!!!", "This is a test message");
		log.info("Order processing email sent");
	}
	
	@KafkaListener(topics = orderCancellationTopic)
	public void consumeOrderCancellation(String message) {
		notificationService.sendEmail("simon.senic@kosickaakademia.sk", "Order CANCELLED!!!", "This is a test message");
		log.info("Order cancellation email sent");
	}
}
