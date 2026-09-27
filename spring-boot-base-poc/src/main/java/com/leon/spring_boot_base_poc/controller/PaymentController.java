package com.leon.spring_boot_base_poc.controller;

import com.leon.spring_boot_base_poc.dto.request.CreatePaymentRequest;
import com.leon.spring_boot_base_poc.dto.response.CreatePaymentResponse;
import com.leon.spring_boot_base_poc.services.PaymentService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        String paymentId = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CreatePaymentResponse(paymentId, "PUBLISHED"));
    }
}
