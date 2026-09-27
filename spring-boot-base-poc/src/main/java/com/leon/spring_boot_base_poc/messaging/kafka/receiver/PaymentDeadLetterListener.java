package com.leon.spring_boot_base_poc.messaging.kafka.receiver;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Simulates "someone will investigate this": records that exhausted the
 * DefaultErrorHandler's 3 retries land here for logging only, so nothing is
 * silently dropped. Reads plain String key/value (see
 * KafkaDeadLetterConsumerConfig) so it can't fail deserializing a payload
 * that may have landed here precisely because it wasn't valid JSON.
 */
@Slf4j
@Component
public class PaymentDeadLetterListener {

    @KafkaListener(
        topics = "${app.kafka.topics.payment-created}.DLT",
        containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record) {
        log.error(
            "DLT: payment event needs investigation. topic={} partition={} offset={} key={} exceptionMessage={} value={}",
            record.topic(), record.partition(), record.offset(), record.key(),
            headerValue(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE), record.value()
        );
    }

    private String headerValue(ConsumerRecord<String, String> record, String headerName) {
        var header = record.headers().lastHeader(headerName);
        return header == null ? "unknown" : new String(header.value());
    }
}
