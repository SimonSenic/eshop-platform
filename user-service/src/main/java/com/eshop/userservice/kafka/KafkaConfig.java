package com.eshop.userservice.kafka;

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
	
	@Value("${customer-registration.topic.name}")
	private String customerRegistrationTopicName;
	
	@Value("${admin-creation.topic.name}")
	private String adminCreationTopicName;
	
	@Value("${partition.count}")
	private Integer partitionCount;
	
	@Value("${replica.count}")
	private Integer replicaCount;
	
	@Bean
	public NewTopic logTopic() {
		return TopicBuilder.name(logTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
	@Bean
	public NewTopic customerRegistrationTopic() {
		return TopicBuilder.name(customerRegistrationTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
	@Bean
	public NewTopic adminCreationTopic() {
		return TopicBuilder.name(adminCreationTopicName).partitions(partitionCount).replicas(replicaCount).build();
	}
	
}
