package com.leon.spring_boot_base_poc.messaging.kafka.receiver;

import com.leon.spring_boot_base_poc.messaging.kafka.dto.event.CreatePaymentEvent;
import com.leon.spring_boot_base_poc.repository.ProcessedPaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreatePaymentConsumer {

    private final ProcessedPaymentRepository processedPaymentRepository;

    /**
     * concurrency = 3 starts 3 threads consuming payment-created (6 partitions)
     * within THIS instance. Kafka assigns at most one consumer per partition
     * per group, so total threads across all instances of this group are
     * capped at the partition count (6) - anything beyond that sits idle.
     * With 3 here, up to 2 instances can each run 3 threads and use all 6
     * partitions; a 3rd instance would get none.
     *
     * Any exception thrown here propagates to the container, where the
     * DefaultErrorHandler bean (see KafkaErrorHandlingConfig) retries it and,
     * if still failing, routes it to the DLT - do not catch it locally, or
     * that retry/DLT path never runs.
     */
    @KafkaListener(
        topics = "${app.kafka.topics.payment-created}",
        groupId = "${app.kafka.consumer-group}",
        concurrency = "3"
    )
    public void listen(
        CreatePaymentEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        Acknowledgment ack
    ) {
        if (event.isSimulateFailure()) {
            // Poison pill simulation: forces the retry -> DLT path.
            throw new IllegalStateException("Simulated processing failure for paymentId=" + event.getPaymentId());
        }

        if (processedPaymentRepository.isProcessed(event.getPaymentId())) {
            log.info(
                "Skipping duplicate paymentId={}, already processed (partition={}, thread={})",
                event.getPaymentId(), partition, Thread.currentThread().getName()
            );
            ack.acknowledge();
            return;
        }

        sendConfirmationEmail(event, partition);
        processedPaymentRepository.markProcessed(event.getPaymentId());

        // Only commits after the email "send" and the idempotency record both
        // succeed. This is what makes the setup at-least-once rather than
        // at-most-once: if the process crashes between markProcessed() and
        // acknowledge(), or before either, the message is redelivered on
        // restart - hence the idempotency check above being required.
        ack.acknowledge();
    }

    private void sendConfirmationEmail(CreatePaymentEvent event, int partition) {
        log.info(
            "Email sent to {} for payment {} (amount={} {}), partition={}, thread={}",
            event.getCustomerEmail(), event.getPaymentId(), event.getAmount(), event.getCurrency(),
            partition, Thread.currentThread().getName()
        );
    }
}
