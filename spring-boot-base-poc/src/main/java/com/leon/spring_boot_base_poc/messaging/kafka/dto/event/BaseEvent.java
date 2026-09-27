package com.leon.spring_boot_base_poc.messaging.kafka.dto.event;

import java.time.Instant;
import java.util.UUID;

import com.leon.spring_boot_base_poc.messaging.kafka.KafkaVersionEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public abstract class BaseEvent {
    private String version = KafkaVersionEnum.V1.getValue();
    private String timestamp = Instant.now().toString();
    private String eventType;

    /**
     * - Dead Letter Queue Tracking
     * - Outbox Pattern Tracking
     * - Event Sourcing
     * - Prevent deduplication on consumer side.
     */
    private String eventId = UUID.randomUUID().toString();

    private String correlationId; // used for tracing

    public BaseEvent(String eventType, String correlationId) {
        this.eventType = eventType;
        this.correlationId = correlationId;
    }
}
