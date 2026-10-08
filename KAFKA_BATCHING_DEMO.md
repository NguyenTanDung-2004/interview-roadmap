# Kafka Producer Batching Configuration & Demo

## Overview
Kafka producer không gửi messages riêng lẻ lên broker. Thay vào đó, nó **gom (batch) các messages** có cùng **partition** và **topic** lại, rồi gửi chung trong một request.

## Batching Config

### application.yaml
```yaml
spring:
  kafka:
    producer:
      batch-size: 16384          # Batch size tối đa (bytes) - 16KB
      linger-ms: 100             # Thời gian chờ để gom thêm messages (ms)
      acks: all                  # Đợi tất cả in-sync replicas acknowledge
      compression-type: snappy   # Nén batch messages
      properties:
        max.in.flight.requests.per.connection: 5
```

### Batching Logic

Producer gửi batch khi **TỠI TIÊN một** điều kiện được thỏa mãn:

1. **Batch size ≥ 16KB** → Gửi ngay (không chờ)
2. **Timeout linger.ms = 100ms** → Gửi dù batch chưa đủ 16KB

```
Timeline của batch:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Msg 1 arrives → accumulate in buffer
Msg 2 arrives → accumulate in buffer
Msg 3 arrives → accumulate in buffer
...
(100ms timeout) → Gửi batch nếu size < 16KB
         HOẶC
(size ≥ 16KB) → Gửi batch ngay
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## Configuration Details

| Config | Default | Value | Mục đích |
|--------|---------|-------|---------|
| `batch.size` | 16384 | bytes | Trigger batch khi dung lượng đạt ngưỡng |
| `linger.ms` | 0 | ms | Chờ bao lâu để gom thêm messages (0 = không chờ) |
| `acks` | 1 | - | `all` = đợi tất cả replica acknowledge (safe) |
| `compression.type` | none | snappy | Nén batch (tiết kiệm bandwidth) |
| `max.in.flight.requests.per.connection` | 5 | - | Max unacked requests trước khi block |

## Run the Demo

### Cách 1: Run với Spring Profile
```bash
cd spring-boot-base-poc

# Chạy với batching demo profile
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=batching-demo"
```

### Cách 2: Set environment variable
```bash
export SPRING_PROFILES_ACTIVE=batching-demo
./mvnw spring-boot:run
```

## Expected Log Output

Khi chạy demo, bạn sẽ thấy logs như này:

```
[BATCH DEMO] Sending message #1 to topic='payment-created' with key='key-1' | 
             This message will be BATCHED with other messages to same partition | 
             Waiting up to 100ms or until batch reaches 16KB

[BATCH DEMO] Sending message #2 to topic='payment-created' with key='key-2' | ...
[BATCH DEMO] Sending message #3 to topic='payment-created' with key='key-3' | ...
[BATCH DEMO] Sending message #4 to topic='payment-created' with key='key-4' | ...
[BATCH DEMO] Sending message #5 to topic='payment-created' with key='key-5' | ...

(Chờ 100ms...)

[BATCH DEMO] Message #1 DELIVERED after 105ms | Partition=0 | Offset=100 | SerializedSize=45bytes
[BATCH DEMO] Message #2 DELIVERED after 105ms | Partition=0 | Offset=101 | SerializedSize=45bytes
[BATCH DEMO] Message #3 DELIVERED after 105ms | Partition=0 | Offset=102 | SerializedSize=45bytes
[BATCH DEMO] Message #4 DELIVERED after 105ms | Partition=0 | Offset=103 | SerializedSize=45bytes
[BATCH DEMO] Message #5 DELIVERED after 105ms | Partition=0 | Offset=104 | SerializedSize=45bytes
```

### Giải thích logs

1. **Sending message** - Message vừa được thêm vào buffer (chưa gửi)
2. **DELIVERED after 105ms** - Tất cả 5 messages delivered cùng lúc (sau 100ms timeout)
   - Nếu time ~ nhau → **Messages được batch lại**
   - Nếu time khác → **Batch được gửi riêng**

## Scenario Analysis

### Scenario 1: Gửi 5 messages nhỏ (< 16KB tổng cộng)

```
Time:     0ms   5ms  10ms  15ms  20ms  ...  100ms
Send:     M1    M2   M3    M4    M5          
Buffer:   [M1]  [M1,M2] [M1-M3] [M1-M4] [M1-M5] ... [timeout!]
Action:                                               SEND ALL 5
```

**Kết quả**: Tất cả 5 messages trong 1 batch
- delivery time ~ nhau (khoảng 100ms)
- Offset liên tục: 100, 101, 102, 103, 104

### Scenario 2: Gửi 20 messages lớn (tổng > 16KB)

```
Time:     0ms   2ms  4ms  6ms  8ms ... 32ms
Send:     M1    M2   M3   M4   M5  ... M16
Buffer:   [M1]  [M1-M2] ... [M1-M7 = ~16KB!]
Action:                    SEND (size trigger)     SEND (size trigger)
```

**Kết quả**: Nhiều batches
- Batch 1: M1-M7 (16KB) → delivery ~10ms
- Batch 2: M8-M15 (16KB) → delivery ~20ms  
- Batch 3: M16-M20 (5KB) → delivery ~100ms (timeout)

## Metric Interpretation

### Delivery Time
- **10-20ms**: Batch triggered bởi size (16KB)
- **100-110ms**: Batch triggered bởi linger timeout

### SerializedSize
- Kích thước từng message sau serialization
- Tổng của batch ≈ sum(SerializedSize) + overhead

### Partition
- Mỗi topic có nhiều partitions
- Messages cùng key → cùng partition
- Messages khác partition → khác batch

## Best Practices

### High Throughput (high latency tolerance)
```yaml
batch-size: 32768    # 32KB
linger-ms: 100       # Chờ 100ms
acks: 1              # Nhanh hơn (không đợi all replicas)
```

### Low Latency (low throughput)
```yaml
batch-size: 16384
linger-ms: 0         # Không chờ
acks: all            # Safe nhưng chậm
```

### Balanced
```yaml
batch-size: 16384    # Default
linger-ms: 10        # Chờ ít (10ms instead 100ms)
acks: 1              # Good balance
```

## Files Created/Modified

- ✅ `spring-boot-base-poc/src/main/resources/application.yaml` - Batching config
- ✅ `spring-boot-base-poc/src/main/java/com/leon/spring_boot_base_poc/messaging/kafka/config/KafkaProducerConfig.java` - Producer factory config
- ✅ `spring-boot-base-poc/src/main/java/com/leon/spring_boot_base_poc/messaging/kafka/producer/CustomKafkaProducer.java` - Enhanced with batch logging
- ✅ `spring-boot-base-poc/src/main/java/com/leon/spring_boot_base_poc/messaging/kafka/example/BatchingDemoExample.java` - Demo executable

## References

- [Kafka Producer Batching](https://kafka.apache.org/documentation/#producerconfigs)
- [linger.ms](https://kafka.apache.org/documentation/#producerconfigs_linger.ms)
- [batch.size](https://kafka.apache.org/documentation/#producerconfigs_batch.size)
- [acks](https://kafka.apache.org/documentation/#producerconfigs_acks)
