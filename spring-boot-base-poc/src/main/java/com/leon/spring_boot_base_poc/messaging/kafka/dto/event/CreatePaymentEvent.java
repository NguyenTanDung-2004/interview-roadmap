package com.leon.spring_boot_base_poc.messaging.kafka.dto.event;

import java.math.BigDecimal;

import com.leon.spring_boot_base_poc.messaging.kafka.EventTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatePaymentEvent extends BaseEvent {
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String customerEmail;

    /** Set by callers to force the consumer to throw, to exercise the retry/DLT path. */
    private boolean simulateFailure;

    public CreatePaymentEvent(
        String correlationId, String paymentId, BigDecimal amount, String currency, String customerEmail, boolean simulateFailure
    ) {
        super(EventTypeEnum.CREATE_PAYMENT_EVENT.getValue(), correlationId);
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.customerEmail = customerEmail;
        this.simulateFailure = simulateFailure;
    }
}
