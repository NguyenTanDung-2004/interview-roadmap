package com.leon.spring_boot_base_poc.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {
    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    @Email
    private String customerEmail;

    /** Optional: pass the same value twice to manually test idempotency (skips duplicate detection otherwise, since one is generated per call). */
    private String paymentId;

    /** For manual testing: forces the consumer to throw, exercising the retry/DLT path. */
    private boolean simulateFailure;
}
