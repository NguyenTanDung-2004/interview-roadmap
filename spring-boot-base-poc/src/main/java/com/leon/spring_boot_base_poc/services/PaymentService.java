package com.leon.spring_boot_base_poc.services;

import java.util.UUID;

import com.leon.spring_boot_base_poc.dto.request.CreatePaymentRequest;
import com.leon.spring_boot_base_poc.messaging.kafka.dto.event.CreatePaymentEvent;
import com.leon.spring_boot_base_poc.messaging.kafka.producer.CreatePaymentProducer;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CreatePaymentProducer createPaymentProducer;

    public String createPayment(CreatePaymentRequest request) {
        String paymentId = request.getPaymentId() != null ? request.getPaymentId() : UUID.randomUUID().toString();

        CreatePaymentEvent event = new CreatePaymentEvent(
            UUID.randomUUID().toString(),
            paymentId,
            request.getAmount(),
            request.getCurrency(),
            request.getCustomerEmail(),
            request.isSimulateFailure()
        );

        // paymentId as the message key guarantees every event for this payment
        // lands on the same partition, so it's always handled by the same
        // consumer thread within the group - required for the idempotency
        // check to matter and for ordering per payment.
        createPaymentProducer.publish(paymentId, event);
        return paymentId;
    }
}
