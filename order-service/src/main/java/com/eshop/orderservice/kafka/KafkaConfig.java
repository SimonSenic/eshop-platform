/*package com.eshop.orderservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
	@Value("${log.topic.name}")
	private String logTopicName;
	
	@Value("${payment-confirmation.topic.name}")
	private String paymentConfirmationTopicName;
	
	@Value("${order-processing.topic.name}")
	private String orderProcessingTopicName;
	
	@Value("${order-cancellation.topic.name}")
	private String orderCancellationTopicName;
	
	@Value("${partition.count}")
	private Integer partitionCount;
	
	@Value("${replica.count}")
	private Integer replicaCount;
	
	@Bean
	public NewTopic logTopic() {
		return TopicBuilder.name(logTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
	@Bean
	public NewTopic paymentConfirmationTopic() {
		return TopicBuilder.name(paymentConfirmationTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
	@Bean
	public NewTopic orderProcessingTopic() {
		return TopicBuilder.name(orderProcessingTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
	@Bean
	public NewTopic orderCancellationTopic() {
		return TopicBuilder.name(orderCancellationTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
}*/
