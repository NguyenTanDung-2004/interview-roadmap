package com.leon.spring_boot_base_poc.messaging.kafka.producer;

import com.leon.spring_boot_base_poc.messaging.kafka.dto.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Slf4j 
@RequiredArgsConstructor 
public abstract class CustomKafkaProducer<T extends BaseEvent> {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, T event) {
        CompletableFuture<SendResult<String, Object>> future = this.kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                onFailure(topic, key, event, ex);
            } else {
                onSuccess(topic, key, event, result);
            }
        });
    }

    protected void onSuccess(String topic, String key, T event, SendResult <String, Object> result) {}

    protected void onFailure(String topic, String key, T event, Throwable ex) {
        log.error("Publish Failure: " + ex.toString());
    }
}
