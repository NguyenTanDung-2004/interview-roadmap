package com.leon.spring_boot_base_poc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePaymentResponse {
    private String paymentId;
    private String status;
}
