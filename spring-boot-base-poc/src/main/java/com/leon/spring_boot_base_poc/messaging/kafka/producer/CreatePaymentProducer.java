package com.leon.spring_boot_base_poc.messaging.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.leon.spring_boot_base_poc.messaging.kafka.dto.event.CreatePaymentEvent;

@Component 
public class CreatePaymentProducer extends CustomKafkaProducer<CreatePaymentEvent>{
    private final String topic;

    public CreatePaymentProducer(KafkaTemplate<String, Object> kafkaTemplate, 
        @Value ("${app.kafka.topics.payment-created}") String topic
    ) {
        super(kafkaTemplate);
        this.topic = topic;
    }

    public void publish(String key, CreatePaymentEvent event) {
        super.publish(topic, key, event);
    }
}
