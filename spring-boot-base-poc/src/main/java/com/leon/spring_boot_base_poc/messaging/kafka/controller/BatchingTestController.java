package com.leon.spring_boot_base_poc.messaging.kafka.controller;

import com.leon.spring_boot_base_poc.messaging.kafka.dto.event.CreatePaymentEvent;
import com.leon.spring_boot_base_poc.messaging.kafka.producer.CreatePaymentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BatchingTestController {

    private final CreatePaymentProducer paymentProducer;

    @GetMapping("/test/batch")
    public String testBatching(@RequestParam(defaultValue = "10") int count) throws InterruptedException {

        log.info("");
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║           KAFKA BATCHING TEST                         ║");
        log.info("║────────────────────────────────────────────────────────║");
        log.info("║ Sending {} messages as fast as possible                ║", String.format("%2d", count));
        log.info("║ Config: linger-ms=2000, batch-size=16KB               ║");
        log.info("║ Expected: Same BrokerTimestamp = BATCHED!             ║");
        log.info("╚════════════════════════════════════════════════════════╝");
        log.info("");

        long startTime = System.currentTimeMillis();

        // Send messages fast to same partition
        for (int i = 1; i <= count; i++) {
            CreatePaymentEvent event = new CreatePaymentEvent(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    new BigDecimal("100.0").add(BigDecimal.valueOf(i)),
                    "USD",
                    "user" + i + "@example.com",
                    false);

            paymentProducer.publish("payment-created", "batch-test-key", event);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("");
        log.info("✓ All {} messages sent in {}ms", count, totalTime);
        log.info("⏳ Waiting for broker batching (linger-ms=2000)...");
        log.info("");

        Thread.sleep(2500);

        log.info("✓ Done. Check logs - same BrokerTimestamp = batched!");
        log.info("");

        return "Sent " + count + " messages!";
    }
}
