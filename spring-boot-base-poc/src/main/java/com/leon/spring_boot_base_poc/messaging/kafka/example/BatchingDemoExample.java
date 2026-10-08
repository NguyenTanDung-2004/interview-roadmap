package com.leon.spring_boot_base_poc.messaging.kafka.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Demo example để thấy Kafka producer batching behavior
 *
 * Chạy với profile: spring.profiles.active=batching-demo
 *
 * Batching hoạt động như sau:
 * 1. Producer gom nhiều messages cùng partition/topic lại
 * 2. Gửi khi: batch size >= 16KB hoặc linger.ms = 100ms timeout
 * 3. Log sẽ hiện thị: khi message gửi và khi batch được deliver
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("batching-demo")
public class BatchingDemoExample implements CommandLineRunner {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String DEMO_TOPIC = "payment-created";

    @Override
    public void run(String... args) throws Exception {
        log.info("\n\n");
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║          KAFKA PRODUCER BATCHING DEMO STARTING            ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
        log.info("");
        log.info("Configuration:");
        log.info("  • Batch Size: 16KB (16384 bytes)");
        log.info("  • Linger MS: 100ms");
        log.info("  • Acks: all (wait for all in-sync replicas)");
        log.info("  • Compression: snappy");
        log.info("");
        log.info("Scenario 1: Gửi 5 messages nhỏ trong 100ms → Sẽ BATCH lại");
        log.info("          (Producer sẽ chờ 100ms hoặc tới 16KB rồi gửi 1 lần)");
        log.info("");

        demoQuickBatching();

        log.info("");
        log.info("════════════════════════════════════════════════════════════");
        log.info("Scenario 2: Gửi messages liên tục → Sẽ batch khi đạt 16KB");
        log.info("");

        demoLargeBatching();

        log.info("");
        log.info("════════════════════════════════════════════════════════════");
        log.info("✓ Demo hoàn tất. Kiểm tra logs để thấy batching behavior.");
        log.info("");
    }

    /**
     * Demo: Gửi 5 messages nhỏ trong khoảng 50ms
     * → Tất cả sẽ được batch lại vì 100ms chưa hết timeout
     */
    private void demoQuickBatching() throws InterruptedException {
        log.info("[DEMO 1] Gửi 5 messages nhỏ (5ms delay giữa mỗi message):");

        for (int i = 1; i <= 5; i++) {
            String message = "Quick_Message_" + i + "_padding_data_to_see_batching_in_logs";
            log.info("");
            kafkaTemplate.send(DEMO_TOPIC, "key-" + i, message);

            if (i < 5) {
                Thread.sleep(5); // 5ms delay
            }
        }

        log.info("");
        log.info("→ Dự kiến: All 5 messages sẽ được batch lại và gửi 1 lần");
        log.info("           (vì tổng size < 16KB và chưa vượt 100ms timeout)");

        // Chờ để batch được gửi
        Thread.sleep(150);
    }

    /**
     * Demo: Gửi messages liên tục để trigger batch dựa vào size (16KB)
     */
    private void demoLargeBatching() throws InterruptedException {
        log.info("[DEMO 2] Gửi 20 messages với large payload:");

        // Message size ~500 bytes, 20 messages = ~10KB
        String largePayload = generateLargeString(500);

        for (int i = 1; i <= 20; i++) {
            String message = "Large_Message_" + i + "_" + largePayload;
            kafkaTemplate.send(DEMO_TOPIC, "key-" + i, message);

            if (i % 5 == 0) {
                log.info("  ...sent {} messages", i);
            }

            Thread.sleep(2); // 2ms delay
        }

        log.info("");
        log.info("→ Dự kiến: Messages sẽ được batch theo 16KB size:");
        log.info("           - Batch 1: ~15-16KB (messages 1-7)");
        log.info("           - Batch 2: ~15-16KB (messages 8-14)");
        log.info("           - Batch 3: ~5KB (messages 15-20, chờ 100ms rồi timeout)");

        // Chờ để tất cả batches được gửi
        Thread.sleep(200);
    }

    private String generateLargeString(int size) {
        return "x".repeat(size);
    }
}
