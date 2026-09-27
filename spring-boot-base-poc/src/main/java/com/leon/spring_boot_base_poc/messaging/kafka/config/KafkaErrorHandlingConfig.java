package com.leon.spring_boot_base_poc.messaging.kafka.config;

import java.util.Collection;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import lombok.extern.slf4j.Slf4j;

/**
 * Both beans below are picked up automatically by Spring Boot's
 * ConcurrentKafkaListenerContainerFactoryConfigurer and wired into the default
 * listener container factory - no need to redefine the factory bean itself.
 */
@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    /**
     * At-least-once delivery means a "poison pill" (a record whose processing
     * always throws) would otherwise wedge its partition forever, since the
     * offset never advances past it. This retries the record locally 3 times,
     * 1s apart, then hands it to the DeadLetterPublishingRecoverer, which
     * republishes it to "<topic>.DLT" and lets the container move past it.
     *
     * Missing this: an unhandled exception in a @KafkaListener would otherwise
     * be retried indefinitely by the default handler (infinite backoff),
     * blocking that partition permanently.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        FixedBackOff backOff = new FixedBackOff(1_000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // With MANUAL ack modes the container normally leaves offset commits to
        // the listener's ack.acknowledge() call. A record that gets recovered
        // (sent to the DLT) never reaches that code path, so without this flag
        // its offset would never advance and it would be redelivered forever.
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    /**
     * Logs which partitions each consumer thread owns whenever the group
     * rebalances (including on startup) - the "observability" requirement for
     * seeing the active consumer group and per-thread partition assignment.
     */
    @Bean
    public ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>> containerCustomizer() {
        return container -> container.getContainerProperties().setConsumerRebalanceListener(new ConsumerAwareRebalanceListener() {
            @Override
            public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
                log.info(
                    "Consumer group '{}' thread '{}' assigned partitions: {}",
                    container.getGroupId(), Thread.currentThread().getName(), partitions
                );
            }
        });
    }
}
