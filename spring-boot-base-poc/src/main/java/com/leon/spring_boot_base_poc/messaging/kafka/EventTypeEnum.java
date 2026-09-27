package com.leon.spring_boot_base_poc.messaging.kafka;

import lombok.Getter;

@Getter 
public enum EventTypeEnum {
    CREATE_PAYMENT_EVENT("CREATE_PAYMENT_EVENT");
    private String value;

    EventTypeEnum(String value) {
        this.value = value;
    }
}
