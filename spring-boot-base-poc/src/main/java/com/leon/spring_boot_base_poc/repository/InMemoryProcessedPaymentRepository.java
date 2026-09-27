package com.leon.spring_boot_base_poc.repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * Stand-in for a "processed_payments" table (an H2/real DB would work the
 * same way: a unique-key lookup before processing, an insert after).
 * ConcurrentHashMap-backed so it's safe under this instance's 3 listener
 * threads, though Kafka only ever routes a given paymentId to one
 * partition/thread at a time - this mainly guards against redeliveries after
 * a crash/retry and against running multiple instances of this service.
 */
@Repository
public class InMemoryProcessedPaymentRepository implements ProcessedPaymentRepository {

    private final Set<String> processedPaymentIds = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isProcessed(String paymentId) {
        return processedPaymentIds.contains(paymentId);
    }

    @Override
    public void markProcessed(String paymentId) {
        processedPaymentIds.add(paymentId);
    }
}
