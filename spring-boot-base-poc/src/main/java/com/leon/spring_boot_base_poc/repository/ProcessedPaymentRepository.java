package com.leon.spring_boot_base_poc.repository;

public interface ProcessedPaymentRepository {
    boolean isProcessed(String paymentId);

    void markProcessed(String paymentId);
}
