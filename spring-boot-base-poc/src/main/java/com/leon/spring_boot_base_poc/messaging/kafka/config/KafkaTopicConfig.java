package com.leon.spring_boot_base_poc.messaging.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.payment-created}")
    private String paymentCreatedTopic;

    @Value("${app.kafka.topics.partitions}")
    private int partitions;

    @Value("${app.kafka.topics.replication-factor}")
    private short replicationFactor;

    // Spring Boot's auto-configured KafkaAdmin picks up every NewTopic bean and
    // creates it on startup if missing. Without this, auto.create.topics.enable
    // would create the topic with only 1 partition on first publish.
    @Bean
    public NewTopic paymentCreatedTopic() {
        return TopicBuilder.name(paymentCreatedTopic)
            .partitions(partitions)
            .replicas(replicationFactor)
            .build();
    }

    // Same partition count as the source topic: DeadLetterPublishingRecoverer
    // republishes a failed record to the same partition number it came from,
    // so the DLT needs at least that many partitions to exist.
    @Bean
    public NewTopic paymentCreatedDeadLetterTopic() {
        return TopicBuilder.name(paymentCreatedTopic + ".DLT")
            .partitions(partitions)
            .replicas(replicationFactor)
            .build();
    }
}
