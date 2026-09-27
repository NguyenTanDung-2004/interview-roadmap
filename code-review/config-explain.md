# Giải thích config trong `application.yaml`

Mỗi mục dưới đây đi theo format: **Vấn đề cần giải quyết → Config áp dụng → Vì sao giá trị đó**.

File tham chiếu: `spring-boot-base-poc/src/main/resources/application.yaml`

---

## 1. Số lượng partitions của topic

```yaml
app:
  kafka:
    topics:
      partitions: 6
```

**Vấn đề:** Một consumer group chỉ có thể xử lý song song tối đa **N consumer instance = N partition**. Nếu topic chỉ có 1 partition, dù bạn scale service lên 10 pod thì cũng chỉ có 1 pod thực sự nhận message — 9 pod còn lại ngồi không. Đây là điểm nghẽn (bottleneck) khi traffic tăng.

**Config áp dụng:** `partitions: 6` — cho phép scale ngang consumer group `payment-email-service` lên tối đa 6 instance chạy song song, throughput tăng gần tuyến tính theo số instance.

**Vì sao là 6 (không phải 1 hay 100):**
- Nhiều hơn 1 để có chỗ scale, nhưng không quá lớn vì mỗi partition tốn thêm file handle, memory buffer trên broker, và tăng thời gian rebalance khi consumer join/leave group.
- Con số này nên chọn dựa trên: throughput mục tiêu / throughput 1 consumer xử lý được, cộng thêm biên độ để scale trong tương lai gần (vì **tăng partition sau này thì dễ, nhưng giảm thì không thể** — muốn giảm phải tạo topic mới).
- Đây là POC nên 6 là con số "đủ dùng để demo khả năng scale", không phải con số đã qua benchmark thực tế.

---

## 2. Replication factor

```yaml
app:
  kafka:
    topics:
      replication-factor: 1
```

**Vấn đề:** Nếu broker giữ leader của partition chết, mà không có bản sao (replica) nào khác, dữ liệu trong partition đó mất vĩnh viễn.

**Config áp dụng:** `replication-factor: 1` — hiện tại **không có replica**, chỉ có 1 bản duy nhất.

**Vì sao là 1 ở đây:** Vì môi trường hiện tại (`docker-compose.yml`) chỉ chạy **1 broker Kafka** — không thể set replication-factor > 1 khi cluster chỉ có 1 broker (Kafka sẽ báo lỗi `InvalidReplicationFactorException`). Đây là giới hạn của môi trường POC, **không phải giá trị nên dùng ở production**. Production cần tối thiểu 3 broker và `replication-factor: 3` để chịu được mất 1 broker mà không mất dữ liệu.

---

## 3. `bootstrap-servers`

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

**Vấn đề:** Client cần biết địa chỉ ít nhất một broker để lấy metadata (danh sách toàn bộ broker, partition, leader) trước khi produce/consume.

**Config áp dụng:** Trỏ vào broker local (`docker-compose.yml`). Trong thực tế nên trỏ vào 2-3 broker (không cần trỏ hết cluster) để nếu 1 broker chết lúc khởi động, client vẫn connect được broker còn lại để lấy metadata.

---

## 4. Serializer / Deserializer + `ErrorHandlingDeserializer`

```yaml
spring:
  kafka:
    producer:
      key-serializer: ...StringSerializer
      value-serializer: ...JsonSerializer
    consumer:
      key-deserializer: ...ErrorHandlingDeserializer
      value-deserializer: ...ErrorHandlingDeserializer
      properties:
        spring.deserializer.key.delegate.class: ...StringDeserializer
        spring.deserializer.value.delegate.class: ...JsonDeserializer
        spring.json.trusted.packages: com.leon...dto.event
```

**Vấn đề:** Message value được serialize dạng JSON. Nếu 1 message trên topic bị lỗi format (JSON sai cú pháp, hoặc field không map được vào class Java) — gọi là **poison pill** — thì mặc định Kafka client sẽ ném exception **ngay tại bước deserialize, trước khi vào listener**. Exception này nằm ngoài tầm với của `DefaultErrorHandler`/retry/DLT logic (những cái đó chỉ bắt exception xảy ra *trong* listener), nên poll loop bị crash và consumer đứng hình vĩnh viễn tại record đó.

**Config áp dụng:**
- `ErrorHandlingDeserializer` bọc ngoài `JsonDeserializer` thật (`delegate.class`): nếu delegate deserialize lỗi, `ErrorHandlingDeserializer` bắt exception đó, đóng gói vào **record header**, và trả message tiếp tục đi vào listener như bình thường (thay vì crash poll loop).
- Exception được đóng gói đó sau đó lại được `DefaultErrorHandler`/DLT xử lý **giống như 1 exception business logic** ném ra từ `@KafkaListener` — tức là dùng chung 1 pipeline retry + DLT cho cả 2 loại lỗi (lỗi parse và lỗi logic), thay vì phải viết 2 luồng xử lý khác nhau.
- `spring.json.trusted.packages`: whitelist package được phép deserialize vào object Java. Đây là biện pháp chống **deserialization gadget attack** — nếu không giới hạn, JSON payload độc hại có thể ép Jackson khởi tạo class tuỳ ý trên classpath.

---

## 5. `auto-offset-reset: earliest`

```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest
```

**Vấn đề:** Khi 1 consumer group **mới** (chưa từng commit offset nào) join vào topic, Kafka phải quyết định đọc từ đâu: từ đầu topic hay từ cuối (chỉ nhận message mới từ giờ trở đi)?

**Config áp dụng:** `earliest` — consumer group mới sẽ đọc lại **toàn bộ** message còn tồn tại trên topic (theo retention), thay vì bỏ qua message cũ.

**Vì sao chọn `earliest` cho use case này:** Đây là service gửi email khi có payment được tạo (`payment-email-service`) — nếu service down và deploy lại với 1 group-id mới, hoặc offset bị mất, ta muốn **không bỏ sót payment nào cần gửi email xác nhận**, thà xử lý trùng (và xử lý idempotent) còn hơn bỏ sót. Nếu đây là use case dạng "chỉ cần dữ liệu real-time, dữ liệu cũ không còn giá trị" (VD: cập nhật vị trí GPS) thì nên dùng `latest`.

---

## 6. `enable-auto-commit: false` + `ack-mode: manual_immediate`

```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: false
    listener:
      ack-mode: manual_immediate
```

**Vấn đề:** Với auto-commit (mặc định), Kafka tự commit offset theo chu kỳ thời gian (VD mỗi 5s), **không quan tâm** message đó đã xử lý xong hay chưa. Nếu process chết giữa chừng — sau khi offset được auto-commit nhưng trước khi email thực sự được gửi — thì khi restart, consumer đọc từ offset đã commit và **bỏ sót message đó vĩnh viễn** (message coi như đã "xử lý" dù thực tế chưa).

**Config áp dụng:**
- `enable-auto-commit: false`: tắt cơ chế commit tự động theo thời gian.
- `ack-mode: manual_immediate`: offset **chỉ** được commit khi code trong listener chủ động gọi `ack.acknowledge()` — và commit **ngay lập tức, đồng bộ** (không đợi gộp vào lần poll tiếp theo).

**Kết quả:** Đảm bảo tính **crash-safety** — nếu process chết trước khi `acknowledge()` chạy, message đó sẽ được redeliver lại khi consumer khởi động lại (partition chưa "tiến" qua offset đó). Đánh đổi: có thể xử lý trùng 1 message nếu process chết *sau khi* xử lý xong nhưng *trước khi* commit kịp gửi đi — nên logic xử lý (gửi email) cần **idempotent**.

---

## 7. `max-poll-records: 50`

```yaml
spring:
  kafka:
    consumer:
      max-poll-records: 50
```

**Vấn đề:** Mỗi lần gọi `poll()`, consumer có thể nhận về rất nhiều record cùng lúc. Nếu số lượng không giới hạn, thời gian xử lý hết batch đó có thể kéo dài không kiểm soát được, dễ vượt quá `max.poll.interval.ms` → broker nghĩ consumer đã chết → rebalance không cần thiết (rebalance storm).

**Config áp dụng:** `max-poll-records: 50` — giới hạn cứng 50 record/lần poll, giúp thời gian xử lý 1 batch **dự đoán được** (predictable).

**Vì sao 50:** Con số này phải thoả điều kiện `max-poll-records × thời gian xử lý 1 record (worst case) < max.poll.interval.ms`. Với `max.poll.interval.ms = 300000` (300s) và ước lượng gửi email mất vài trăm ms/record (bao gồm cả retry mạng), 50 record để lại biên độ an toàn lớn.

---

## 8. Bộ 3 config timeout: `max.poll.interval.ms`, `session.timeout.ms`, `heartbeat.interval.ms`

```yaml
properties:
  max.poll.interval.ms: 300000
  session.timeout.ms: 45000
  heartbeat.interval.ms: 15000
```

**Vấn đề:** Kafka cần 2 cơ chế độc lập để phát hiện consumer "chết" và trigger rebalance (đẩy partition sang consumer khác):
1. Consumer thread chính bận xử lý message quá lâu, không quay lại gọi `poll()` kịp → nghi ngờ *xử lý bị treo*.
2. Consumer mất kết nối / GC pause / crash → không gửi heartbeat được → nghi ngờ *tiến trình đã chết*.

Nếu detect sai (quá nhạy) → rebalance liên tục dù consumer vẫn khoẻ (chỉ là đang GC pause tạm thời), gây gián đoạn xử lý không cần thiết. Nếu detect quá chậm → consumer chết thật mà partition của nó bị "đóng băng" lâu, message không ai xử lý.

**Config áp dụng — 3 tầng độc lập:**
- `max.poll.interval.ms: 300000` (5 phút): giới hạn thời gian tối đa giữa 2 lần gọi `poll()`. Phải **lớn hơn thoải mái** so với `max-poll-records × worst-case processing time/record` (mục 7) để business logic chậm không bị hiểu nhầm là treo.
- `session.timeout.ms: 45000` (45s): thời gian broker chờ không thấy heartbeat trước khi coi consumer đã chết và rebalance. Không được quá thấp (tránh rebalance giả do GC pause/network blip ngắn) và không được quá cao (phát hiện chết chậm).
- `heartbeat.interval.ms: 15000` (15s): tần suất background thread gửi heartbeat — theo rule-of-thumb **≈ 1/3 của `session.timeout.ms`**, để có ít nhất 2-3 lần heartbeat "cơ hội" trước khi bị coi là timeout (tránh 1 lần mất gói tin làm rớt cả consumer).

---

## 9. `group-id` / `consumer-group: payment-email-service`

```yaml
spring:
  kafka:
    consumer:
      group-id: payment-email-service
app:
  kafka:
    consumer-group: payment-email-service
```

**Vấn đề:** Consumer group-id quyết định việc **chia sẻ tải** (mỗi partition chỉ được đọc bởi 1 consumer trong cùng group) và **cô lập offset** giữa các service khác nhau đọc cùng 1 topic (VD: service gửi email và service ghi audit log cùng đọc topic `payment-created` nhưng phải track offset độc lập).

**Config áp dụng:** Đặt tên group theo **chức năng nghiệp vụ** (`payment-email-service`) chứ không phải theo tên topic — vì 1 topic có thể có nhiều consumer group khác nhau đọc cùng lúc cho mục đích khác nhau, và tên group cần mô tả rõ "ai đang đọc" để dễ trace khi debug lag/rebalance.

---

## 10. Số lượng partitions của DLT (Dead Letter Topic)

Xem `KafkaTopicConfig.java` — DLT (`payment-created.DLT`) được tạo với **cùng số partitions** với topic gốc (`partitions: 6`).

**Vấn đề:** `DeadLetterPublishingRecoverer` mặc định republish message lỗi vào **đúng số partition** mà nó đến từ topic gốc (để giữ nguyên thứ tự tương đối, dễ trace). Nếu DLT có ít partition hơn topic gốc, sẽ có message bị route vào partition không tồn tại → lỗi.

**Config áp dụng:** Dùng chung biến `partitions` (`app.kafka.topics.partitions`) cho cả 2 topic, đảm bảo tính nhất quán và tránh phải nhớ sync tay 2 giá trị riêng biệt.

---

## 11. Retry + Dead Letter Topic (`FixedBackOff`, `commitRecovered`)

Nằm trong `KafkaErrorHandlingConfig.java`, không phải YAML, nhưng liên quan trực tiếp tới cụm config ack-mode/manual-commit ở trên nên nêu ở đây để đủ bức tranh:

**Vấn đề:** Với **at-least-once delivery** + manual ack, nếu 1 message luôn luôn ném exception khi xử lý (poison pill ở tầng business logic, VD: email service down), mà không có cơ chế "bỏ qua sau N lần thử", thì message đó sẽ được retry **vô hạn**, offset không bao giờ tiến, và **toàn bộ partition đó bị nghẽn vĩnh viễn** — mọi message phía sau, kể cả không lỗi, đều không được xử lý.

**Config áp dụng:**
- `FixedBackOff(1000ms, 3 lần)`: retry local ngay tại consumer 3 lần, cách nhau 1s, trước khi bỏ cuộc — đủ để vượt qua lỗi thoáng qua (transient, VD SMTP server timeout 1 lần) mà không giữ partition quá lâu.
- Sau 3 lần thất bại: `DeadLetterPublishingRecoverer` đẩy message sang topic `.DLT` — cô lập message lỗi ra khỏi luồng chính, để consumer tiếp tục xử lý các message tiếp theo, đồng thời không mất dữ liệu (có thể investigate/replay từ DLT sau).
- `setCommitRecovered(true)`: vì đang dùng `MANUAL_IMMEDIATE` ack-mode, listener code không hề biết về message bị recover (nó không đi qua code gọi `ack.acknowledge()`), nên nếu không set flag này, offset của message đó **không bao giờ được commit** → bị redeliver lại vô hạn dù đã đẩy vào DLT thành công.

---

## 12. `management.endpoints.web.exposure.include: health,info`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

**Vấn đề:** Actuator mặc định (khi có trên classpath) chỉ expose `health` và `info` qua HTTP — nhưng nhiều dự án bật thêm nhiều endpoint (`env`, `beans`, `metrics`...) một cách mặc định, có thể lộ thông tin nhạy cảm (biến môi trường, cấu hình nội bộ) nếu không kiểm soát.

**Config áp dụng:** Khai báo **tường minh** (explicit) chỉ 2 endpoint cần cho mục đích cơ bản:
- `health`: để load balancer / k8s liveness-readiness probe biết service còn sống không.
- `info`: thông tin build/version cơ bản.

Đây là practice "expose tối thiểu cần thiết" thay vì dựa vào default của framework — tránh vô tình lộ endpoint nhạy cảm khi thêm dependency mới.
