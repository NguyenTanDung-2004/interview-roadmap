package com.leon.spring_boot_base_poc.messaging.kafka.producer;

import com.leon.spring_boot_base_poc.messaging.kafka.dto.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Slf4j
@RequiredArgsConstructor
public abstract class CustomKafkaProducer<T extends BaseEvent> {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final AtomicInteger messageCounter = new AtomicInteger(0);

    public void publish(String topic, String key, T event) {
        int msgNum = messageCounter.incrementAndGet();
        long sendTime = System.currentTimeMillis();

        log.info("[BATCH TEST] Sending message #{} to topic='{}' with key='{}' | " +
                 "Producer will batch this with other messages in same partition",
                 msgNum, topic, key);

        CompletableFuture<SendResult<String, Object>> future = this.kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            long deliveryTime = System.currentTimeMillis() - sendTime;
            if (ex != null) {
                log.error("[BATCH TEST] Message #{} FAILED after {}ms", msgNum, deliveryTime);
                onFailure(topic, key, event, ex);
            } else {
                int partition = result.getRecordMetadata().partition();
                long offset = result.getRecordMetadata().offset();
                long brokerTimestamp = result.getRecordMetadata().timestamp();

                log.info("[BATCH TEST] Message #{} DELIVERED | " +
                         "Partition={} | Offset={} | BrokerTimestamp={} | DeliveryTime={}ms",
                         msgNum, partition, offset, brokerTimestamp, deliveryTime);

                onSuccess(topic, key, event, result);
            }
        });
    }

    protected void onSuccess(String topic, String key, T event, SendResult<String, Object> result) {}

    protected void onFailure(String topic, String key, T event, Throwable ex) {
        log.error("Publish Failure: " + ex.toString());
    }
}
