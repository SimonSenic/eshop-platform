package com.eshop.storageservice.kafka;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class KafkaConfig {
	private KafkaProperties kafkaProperties;
	
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
	public NewTopic topic() {
		return TopicBuilder.name("product").partitions(1).replicas(1).build();
	}
	
}
