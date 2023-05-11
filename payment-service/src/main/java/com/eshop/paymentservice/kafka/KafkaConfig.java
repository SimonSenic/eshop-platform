package com.eshop.paymentservice.kafka;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
	@NonNull
	private final KafkaProperties kafkaProperties;
	
	@Value("${payment.topic.name}")
	private String paymentTopicName;
	
	@Value("${log.topic.name}")
	private String logTopicName;
	
	@Value("${partition.count}")
	private Integer partitionCount;
	
	@Value("${replica.count}")
	private Integer replicaCount;
	
	@Bean
	public ProducerFactory<String, String> producerFactory(){
		Map<String, Object> properties = kafkaProperties.buildProducerProperties();
		return new DefaultKafkaProducerFactory<>(properties);
	}
	
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate(){
		return new KafkaTemplate<String, String>(producerFactory());
	}
	
	@Bean
	public NewTopic paymentTopic() {
		return TopicBuilder.name(paymentTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
	@Bean
	public NewTopic logTopic() {
		return TopicBuilder.name(logTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}

}
