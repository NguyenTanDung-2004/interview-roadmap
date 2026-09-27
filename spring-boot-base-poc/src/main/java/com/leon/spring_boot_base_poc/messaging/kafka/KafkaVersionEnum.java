package com.leon.spring_boot_base_poc.messaging.kafka;

public enum KafkaVersionEnum {
    V1("v1");

    private String value;

    public String getValue() {
        return this.value;
    }

    KafkaVersionEnum(String value) {
        this.value = value;
    }
}
