# Kafka Payment Consumer — Lộ trình học (chia theo session)

Đi kèm với [`summarize.md`](./summarize.md) và `expectations/requirement.md`. File đó nói
*đã làm gì*; file này nói *học lại như thế nào* mà không phải nhồi hết một lúc. Mỗi session
khoảng 45–60 phút: đọc code, tự trả lời câu hỏi (giả lập phỏng vấn), rồi làm phần hands-on
trước khi qua session tiếp theo.

**Nguyên tắc xuyên suốt cho mọi config:** với mỗi cấu hình, luôn đi theo trình tự
**Vấn đề thực tế → Kafka/Spring Kafka hỗ trợ gì → Tại sao chọn giá trị này ở đây**.
Đừng học thuộc tên config — học lý do nó tồn tại.

Tick vào ô khi bạn có thể trả lời hết câu hỏi của session mà không cần nhìn code.

---

## Session 1 — Đường đi của Producer: REST → Service → Kafka

**Đọc theo thứ tự:**
- `controller/PaymentController.java`
- `dto/request/CreatePaymentRequest.java`, `dto/response/CreatePaymentResponse.java`
- `services/PaymentService.java`
- `messaging/kafka/dto/event/BaseEvent.java`, `.../event/CreatePaymentEvent.java`
- `messaging/kafka/producer/CustomKafkaProducer.java`, `.../producer/CreatePaymentProducer.java`
- `messaging/kafka/config/KafkaTopicConfig.java`
- `application.yaml` — chỉ phần `spring.kafka.producer` + `app.kafka.topics`

**Vấn đề → Cấu hình:**

- **Vấn đề:** nếu không chỉ định key khi gửi message, Kafka sẽ round-robin (hoặc hash rỗng)
  message qua các partition — 2 message của cùng một `paymentId` (ví dụ: tạo payment rồi
  cập nhật trạng thái) có thể rơi vào 2 partition khác nhau, do 2 consumer thread khác nhau
  xử lý, không đảm bảo thứ tự và phá luôn logic idempotency theo từng payment.
  → **Cấu hình:** `kafkaTemplate.send(topic, key, event)` với `key = paymentId`
  (`PaymentService.java`, dòng dùng `paymentId` làm key).
  → **Tại sao:** Kafka hash key để chọn partition, nên cùng key luôn về cùng partition ⇒
  cùng một consumer thread trong group xử lý ⇒ giữ thứ tự theo `paymentId` và làm cho check
  idempotency ở Session 4 có ý nghĩa (không bị 2 thread race trên cùng 1 `paymentId`).

- **Vấn đề:** nếu để Kafka tự tạo topic khi producer gửi message đầu tiên
  (`auto.create.topics.enable=true`, mặc định), topic sẽ được tạo với **1 partition** —
  không đủ cho yêu cầu scale với `concurrency = 3` ở consumer.
  → **Cấu hình:** khai báo bean `NewTopic` qua `TopicBuilder` trong `KafkaTopicConfig.java`
  với `partitions(6)`, `replicas(1)`.
  → **Tại sao:** Spring Boot's `KafkaAdmin` tự động lấy mọi bean `NewTopic` và tạo topic lúc
  startup nếu chưa tồn tại — chủ động định nghĩa partition count thay vì phó mặc cho default.

**Tự kiểm tra:**
- [ ] Nếu dùng `customerEmail` làm key thay vì `paymentId` thì sẽ gây ra vấn đề gì trong việc phân phối message qua các partition?
- [ ] `CreatePaymentRequest.paymentId` và `.simulateFailure` không nằm trong spec field list — chúng tồn tại để làm gì, và vì sao chấp nhận được trong PoC nhưng không nên có ở production?
- [ ] Nếu xóa hẳn `KafkaTopicConfig`, topic sẽ được tạo với bao nhiêu partition khi bạn gọi `/payments` lần đầu?
- [ ] `CustomKafkaProducer.publish()` là fire-and-forget ngoại trừ callback `whenComplete` — `onFailure` hiện tại chỉ log, vậy làm sao biết một payment event đã bị mất thật sự?

**Hands-on:** chưa cần chạy gì với Kafka ở session này. Có thể start app (chưa cần Kafka)
và thử `POST /payments` với body thiếu `currency` để xác nhận `@Valid` trả về 400.

---

## Session 2 — Consumer cơ bản: `@KafkaListener`, consumer group, concurrency

**Đọc:**
- `messaging/kafka/receiver/CreatePaymentConsumer.java` (bỏ qua phần idempotency và
  `simulateFailure` — để dành Session 3–5)
- `application.yaml` — phần `spring.kafka.consumer` (bỏ qua các tuning property, để Session 6)
- `EventTypeEnum.java`, `KafkaVersionEnum.java` (đọc nhanh cho quen)

**Vấn đề → Cấu hình:**

- **Vấn đề:** một instance chỉ dùng 1 thread để đọc Kafka thì throughput bị giới hạn —
  6 partition mà chỉ 1 thread đọc tuần tự thì rất chậm khi traffic tăng.
  → **Cấu hình:** `@KafkaListener(..., concurrency = "3")`.
  → **Tại sao:** Spring Kafka tạo 3 consumer thread trong cùng instance, mỗi thread nhận
  một tập partition riêng — miễn là số thread ≤ số partition (Kafka chỉ gán tối đa 1
  consumer/partition/group, nên thread dư ra sẽ ngồi không).

- **Vấn đề:** nếu deploy nhiều instance của service mà không group chúng lại, mỗi instance
  sẽ đọc **toàn bộ** message độc lập ⇒ email bị gửi trùng nhiều lần cho cùng 1 payment.
  → **Cấu hình:** `group-id: payment-email-service` (dùng chung giữa các instance).
  → **Tại sao:** Kafka đảm bảo trong cùng 1 group, mỗi partition chỉ được đọc bởi đúng 1
  consumer tại một thời điểm — đây là cơ chế native để scale ngang mà không lo trùng lặp ở
  tầng partition (còn trùng lặp do retry/crash thì vẫn cần idempotency ở Session 4).

**Tự kiểm tra:**
- [ ] `payment-created` có 6 partition, `concurrency = 3`. Giải thích chính xác việc phân bổ partition sẽ ra sao nếu chạy **1 instance** so với **2 instance**, cùng group `payment-email-service`.
- [ ] Vì sao concurrency bị giới hạn bởi số partition, chứ không phải bởi số CPU core hay thread pool size?
- [ ] Consumer group thực chất giải quyết vấn đề gì? Nếu mỗi instance dùng `group-id` khác nhau thì hệ quả là gì?
- [ ] Listener đọc `@Header(KafkaHeaders.RECEIVED_PARTITION) int partition` — log kèm partition + thread name để làm gì? Nó giúp bạn chẩn đoán điều gì khi có rebalance?
- [ ] `key-deserializer`/`value-deserializer` là `ErrorHandlingDeserializer`, không phải `StringDeserializer`/`JsonDeserializer` trực tiếp — cái nào đang delegate cho cái nào (xem `spring.deserializer.*.delegate.class`)? (Hiểu sâu ở Session 5, ở đây chỉ cần nắm hình dạng.)

**Hands-on:**
```bash
docker compose up -d          # chạy từ thư mục gốc repo
# kiểm tra broker đã lên:
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```
Start app, gọi `POST /payments` với body hợp lệ, quan sát log từ `sendConfirmationEmail` —
ghi lại partition và thread name được in ra.

---

## Session 3 — Manual acknowledgment & crash-safety

**Đọc:**
- `CreatePaymentConsumer.java` — lần này tập trung vào tham số `Acknowledgment ack` và vị trí gọi `ack.acknowledge()`
- `application.yaml` — `enable-auto-commit: false` và `listener.ack-mode: manual_immediate`

**Vấn đề → Cấu hình:**

- **Vấn đề:** mặc định Kafka consumer tự động commit offset theo chu kỳ (`enable.auto.commit=true`),
  bất kể message đã xử lý xong hay chưa. Nếu app crash **sau khi** offset được auto-commit
  nhưng **trước khi** email thật sự được gửi/log xong, message đó coi như bị mất — offset
  đã trôi qua rồi nên khi restart Kafka sẽ không gửi lại.
  → **Cấu hình:** `enable-auto-commit: false`.
  → **Tại sao:** tắt cơ chế commit ngầm định kỳ, để việc commit offset hoàn toàn nằm trong
  tay code — chỉ commit khi mình chắc chắn đã xử lý xong.

- **Vấn đề:** chỉ tắt auto-commit thôi thì Spring Kafka container mặc định vẫn tự quyết định
  thời điểm commit (theo batch, theo record cuối của 1 lần poll) — vẫn chưa đủ granular để
  gắn chính xác vào logic nghiệp vụ (gửi email xong mới commit).
  → **Cấu hình:** `listener.ack-mode: manual_immediate` + tham số `Acknowledgment ack` trong
  listener, gọi `ack.acknowledge()` là bước cuối cùng sau khi `sendConfirmationEmail` và
  `markProcessed` chạy xong.
  → **Tại sao:** `MANUAL_IMMEDIATE` giao toàn quyền commit cho code, và commit **ngay lập
  tức, đồng bộ** từng record thay vì gom batch ở lần poll sau — nếu process chết trước dòng
  `ack.acknowledge()`, offset chưa commit ⇒ Kafka gửi lại message khi consumer group
  rebalance/restart. Đây chính là cơ chế tạo ra **at-least-once**, không phải at-most-once.

**Tự kiểm tra:**
- [ ] `enable-auto-commit: false` một mình thay đổi điều gì? `ack-mode: manual_immediate` thêm vào điều gì nữa? (Đây là 2 knob riêng biệt, đừng gộp làm một.)
- [ ] Trong `CreatePaymentConsumer.listen()`, `ack.acknowledge()` được gọi **sau** `sendConfirmationEmail` và `markProcessed`. Thiết kế này để lộ khoảng hở lỗi (failure window) cụ thể nào, và vì sao vẫn chấp nhận được ("at-least-once" chứ không phải "exactly-once")?
- [ ] Nếu `ack.acknowledge()` là dòng **đầu tiên** thay vì dòng cuối, guarantee sẽ đổi thành gì? Vì sao tệ hơn hẳn ở use case này?
- [ ] `MANUAL_IMMEDIATE` commit đồng bộ theo từng record thay vì gộp batch — cái giá phải trả về throughput là gì, và vì sao vẫn đáng đánh đổi với use case gửi email thanh toán?

**Hands-on:** kill process app (`Ctrl+C` hoặc `kill -9`) ngay sau khi thấy log "Email sent"
nhưng trước khi kịp commit offset, rồi restart. Xem log để xác nhận message có bị gửi lại
hay không. Lưu ý: với code hiện tại, lúc log in ra thì `markProcessed` + `ack.acknowledge()`
gần như đã chạy xong — muốn thấy rõ hơn có thể tạm thêm `Thread.sleep` để "win race", hoặc
đơn giản là suy luận qua code thay vì cố canh đúng thời điểm.

---

## Session 4 — Idempotency (chống gửi email trùng)

**Đọc:**
- `repository/ProcessedPaymentRepository.java`
- `repository/InMemoryProcessedPaymentRepository.java`
- `CreatePaymentConsumer.java` — đoạn `isProcessed` / `markProcessed`

**Vấn đề → Cấu hình:**

- **Vấn đề:** at-least-once (Session 3) đồng nghĩa với việc **chắc chắn sẽ có lúc** một
  message được xử lý lại (do crash giữa chừng, do retry sau lỗi tạm thời...). Kafka **không**
  có cơ chế nào ở tầng consumer để tự động phát hiện "message này tôi xử lý rồi" — dedup
  không phải việc của Kafka, mà của consumer group coordination (chỉ đảm bảo *ai đọc*, không
  đảm bảo *đọc bao nhiêu lần*).
  → **Cấu hình (ở tầng application, không phải Kafka):** `ProcessedPaymentRepository` — check
  `isProcessed(paymentId)` trước khi gửi email, `markProcessed(paymentId)` ngay sau khi gửi
  xong, trước khi ack.
  → **Tại sao:** đây là lý do "delivery guarantee = at-least-once" ở tầng hạ tầng luôn đi kèm
  yêu cầu "idempotency ở tầng application" trong thiết kế hệ thống dùng message queue —
  không có cách nào Kafka tự lo phần này thay bạn.

- **Vấn đề:** 3 thread (`concurrency = 3`) cùng truy cập một cấu trúc dữ liệu dùng chung —
  nếu dùng `HashSet` thường, thao tác `add`/`contains` không thread-safe, có thể corrupt dữ
  liệu nội bộ hoặc trả về kết quả sai dưới truy cập đồng thời.
  → **Cấu hình:** `ConcurrentHashMap.newKeySet()`.
  → **Tại sao:** dù về lý thuyết mỗi `paymentId` chỉ luôn rơi vào đúng 1 partition/1 thread
  (nhờ Session 1 dùng key), việc dùng cấu trúc thread-safe vẫn cần thiết để phòng ngừa lỗi ở
  tầng JVM (visibility giữa các thread) và để an toàn khi redelivery xảy ra gần như đồng thời
  với việc chạy nhiều instance.

**Tự kiểm tra:**
- [ ] Vì sao at-least-once delivery **bắt buộc** phải có check idempotency ở tầng application? Kafka không đảm bảo điều gì ở đây?
- [ ] Vì sao dùng `ConcurrentHashMap.newKeySet()` thay vì `HashSet` thường, trong khi comment nói chỉ 1 thread từng chạm vào 1 `paymentId`? Nó thực sự phòng ngừa tình huống nào?
- [ ] Repository này in-memory và reset khi restart. Bug thực tế nào sẽ tái xuất hiện khi đó, và sẽ khác gì nếu backing bằng H2/Postgres?
- [ ] Giữa `isProcessed()` và `markProcessed()` có khoảng hở check-then-act (không atomic). Trong điều kiện nào khoảng hở này có thể để lọt email trùng, và vì sao trong setup cụ thể này (mỗi key về đúng 1 partition + 1 thread) nó không thành vấn đề?

**Hands-on:**
```bash
curl -X POST localhost:8080/payments -H 'Content-Type: application/json' \
  -d '{"amount":10.00,"currency":"USD","customerEmail":"a@test.com","paymentId":"dup-1"}'
# gọi lại đúng curl này lần nữa
```
Xác nhận lần gọi thứ 2 log ra "Skipping duplicate" thay vì gửi email lần nữa.

---

## Session 5 — Poison pill, error handling, Dead Letter Topic

Session nặng nhất — có thể tách thành 5a (retry/DLT ở tầng business logic) và 5b (lỗi ở
tầng deserialize JSON) nếu thấy dồn quá nhiều trong một buổi.

**Đọc (5a — lỗi business logic):**
- `messaging/kafka/config/KafkaErrorHandlingConfig.java` (cả 2 bean)
- `CreatePaymentConsumer.java` — nhánh `simulateFailure`
- `dto/request/CreatePaymentRequest.java` / `CreatePaymentEvent.java` — field `simulateFailure`

**Đọc (5b — lỗi tầng deserialize):**
- `application.yaml` — dòng `ErrorHandlingDeserializer` + `spring.deserializer.*.delegate.class`
- `messaging/kafka/config/KafkaDeadLetterConsumerConfig.java`
- `messaging/kafka/receiver/PaymentDeadLetterListener.java`

**Vấn đề → Cấu hình:**

- **Vấn đề (poison pill):** nếu một message luôn khiến listener throw exception (bug logic,
  data hỏng...), và không có cơ chế retry/backoff nào, Spring Kafka mặc định sẽ retry với
  **backoff vô hạn** ⇒ partition đó bị "kẹt cứng" mãi mãi, offset không bao giờ tiến — toàn
  bộ payment sau đó trên partition đó cũng bị chặn theo (vì Kafka đọc tuần tự trong 1 partition).
  → **Cấu hình:** `DefaultErrorHandler` với `FixedBackOff(1_000L, 3)` — retry 3 lần, cách nhau 1s.
  → **Tại sao:** giới hạn số lần retry để không kẹt vô hạn, nhưng vẫn cho cơ hội tự hồi phục
  nếu lỗi chỉ là tạm thời (network blip, DB tạm timeout...).

- **Vấn đề:** sau khi retry hết 3 lần mà vẫn lỗi, phải có nơi "đẩy" message đó ra khỏi đường
  xử lý chính — nếu không, offset vẫn không tiến được và ta quay lại đúng vấn đề "kẹt partition".
  → **Cấu hình:** `DeadLetterPublishingRecoverer` gắn vào `DefaultErrorHandler`, publish
  message lỗi sang topic `<topic>.DLT`, giữ nguyên partition number qua lambda
  `(record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())`.
  → **Tại sao:** cho phép container bỏ qua message lỗi và tiến offset tiếp, đồng thời không
  làm mất message — nó được lưu lại ở DLT để người vận hành xem xét sau (đúng yêu cầu "someone
  will investigate this"). DLT cần có **số partition ≥ topic gốc** vì recoverer giữ nguyên số
  partition khi publish sang.

- **Vấn đề:** ở `ack-mode: manual_immediate`, việc commit offset hoàn toàn phụ thuộc vào code
  gọi `ack.acknowledge()` — nhưng khi message được recover (đẩy sang DLT) thì code listener
  **không hề chạy tới dòng `ack.acknowledge()`** (exception đã văng ra trước đó). Nếu không có
  gì khác commit offset đó, message sẽ được coi là "chưa xử lý" mãi mãi ⇒ bị đọc lại vô hạn.
  → **Cấu hình:** `errorHandler.setCommitRecovered(true)`.
  → **Tại sao:** báo cho error handler biết "khi đã recover (đẩy DLT) thành công thì coi như
  offset này xong, tự commit giúp" — nếu để `false`, message vẫn bị redeliver lặp lại vô hạn
  dù đã nằm trong DLT rồi (vừa lãng phí vừa gây nhiễu log).

- **Vấn đề:** không phải lúc nào lỗi cũng nằm ở business logic — một payload JSON bị hỏng
  (không parse được thành `CreatePaymentEvent`) sẽ throw exception ngay ở tầng **deserialize**,
  tức là **trước khi** message đến được listener. Nếu dùng `JsonDeserializer` trực tiếp, lỗi
  này sẽ làm **crash cả poll loop** của consumer thread — không chỉ 1 message bị ảnh hưởng mà
  cả partition đó ngừng đọc hoàn toàn.
  → **Cấu hình:** bọc `key-deserializer`/`value-deserializer` bằng `ErrorHandlingDeserializer`,
  rồi trỏ `spring.deserializer.key/value.delegate.class` về deserializer thật
  (`StringDeserializer`/`JsonDeserializer`).
  → **Tại sao:** `ErrorHandlingDeserializer` bắt lỗi deserialize ngay tại chỗ, đóng gói lỗi vào
  record header thay vì throw thẳng ra làm chết poll loop — nhờ vậy lỗi deserialize được đưa
  vào **cùng con đường retry/DLT** với lỗi business logic, xử lý thống nhất một chỗ.

- **Vấn đề:** message rơi vào DLT vì lý do JSON không hợp lệ — nếu consumer đọc DLT lại cố
  deserialize nó thành `CreatePaymentEvent` (dùng cùng factory với consumer chính), nó sẽ lại
  lỗi y hệt, tạo vòng lặp "lỗi khi xử lý message lỗi".
  → **Cấu hình:** `KafkaDeadLetterConsumerConfig` định nghĩa riêng `ConsumerFactory<String, String>`
  + `ConcurrentKafkaListenerContainerFactory` dùng `StringDeserializer` thuần cho cả key/value,
  và `PaymentDeadLetterListener` đọc `ConsumerRecord<String, String>`.
  → **Tại sao:** đọc raw String thì không bao giờ fail deserialize nữa cho dù payload là gì —
  đảm bảo consumer log-cho-người-điều-tra này không bao giờ tự sập vì chính lý do message kia
  bị đưa vào DLT.

**Tự kiểm tra:**
- [ ] Đi từng bước điều gì xảy ra khi `listen()` throw: bean nào bắt lỗi, `FixedBackOff(1_000L, 3)` làm gì, và sau lần retry thứ 3 thất bại thì điều gì xảy ra tiếp theo?
- [ ] Vì sao DLT topic cần có **số partition ít nhất bằng** topic gốc?
- [ ] `errorHandler.setCommitRecovered(true)` làm gì, và bạn sẽ quan sát thấy hiện tượng gì (loop vô hạn? mất dữ liệu âm thầm?) nếu để `false`, biết rằng đang dùng `ack-mode: manual_immediate`?
- [ ] Có 2 con đường khác nhau khiến 1 record rơi vào `payment-created.DLT`: exception bị throw trong `listen()`, và lỗi deserialize JSON. Giải thích từng con đường, và vì sao `PaymentDeadLetterListener` đọc `ConsumerRecord<String, String>` (String thuần) thay vì `ConsumerRecord<String, CreatePaymentEvent>`.
- [ ] Nếu bỏ `ErrorHandlingDeserializer` và quay về `JsonDeserializer` thường, điều gì sẽ xảy ra với **cả poll loop** (không chỉ 1 message) khi gặp payload JSON hỏng?

**Hands-on — poison pill ở tầng business logic:**
```bash
curl -X POST localhost:8080/payments -H 'Content-Type: application/json' \
  -d '{"amount":10.00,"currency":"USD","customerEmail":"a@test.com","simulateFailure":true}'
```
Theo dõi log: 3 lần retry cách nhau ~1s, sau đó có dòng log `DLT:` từ `PaymentDeadLetterListener`.

**Hands-on — poison pill ở tầng deserialize (JSON hỏng, gửi thẳng):**
```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic payment-created
> this is not valid json
```
Xác nhận message này cũng vào được DLT (qua đường `ErrorHandlingDeserializer`, không cần
retry vì chưa từng tới listener), và poll loop của app vẫn chạy bình thường sau đó — gọi
`POST /payments` một payment hợp lệ ngay sau để xác nhận vẫn được xử lý.

---

## Session 6 — Tuning & Observability

**Đọc:**
- `application.yaml` — 4 tuning property kèm comment
  (`max-poll-records`, `max.poll.interval.ms`, `session.timeout.ms`, `heartbeat.interval.ms`)
- `KafkaErrorHandlingConfig.java` — bean `containerCustomizer()`
- Block `management.endpoints.web.exposure`

**Vấn đề → Cấu hình:**

- **Vấn đề:** một lần `poll()` mặc định có thể trả về rất nhiều record — nếu số lượng record
  xử lý trong 1 lần poll quá lớn, tổng thời gian xử lý có thể vượt quá thời gian broker cho
  phép giữa 2 lần poll, khiến consumer bị coi là "chết" và bị đá khỏi group.
  → **Cấu hình:** `max-poll-records: 50`.
  → **Tại sao:** giới hạn số record mỗi lần poll giúp thời gian xử lý một batch dự đoán được,
  dễ tính toán để không vượt `max.poll.interval.ms`.

- **Vấn đề:** nếu xử lý 1 batch record mất quá lâu (business logic chậm, DB lag...) mà broker
  không có giới hạn nào, broker sẽ không biết consumer có còn sống hay đã "treo" — nhưng nếu
  giới hạn quá chặt, một batch xử lý hơi lâu bình thường cũng bị coi là chết oan, gây
  rebalance không cần thiết (mất hết state đang xử lý dở, duplicate tăng lên).
  → **Cấu hình:** `max.poll.interval.ms: 300000` (5 phút).
  → **Tại sao:** đây là "hạn chót" giữa 2 lần gọi `poll()` — phải **lớn hơn thoải mái** so với
  `max-poll-records × thời gian xử lý 1 record trong tình huống xấu nhất`, để không rebalance
  oan khi xử lý đang chạy bình thường nhưng hơi chậm.

- **Vấn đề:** nếu một consumer instance bị crash/treo (network chết, GC pause quá lâu, process
  bị kill), broker cần phát hiện để rebalance partition của nó sang consumer khác — nhưng phát
  hiện dựa trên tiêu chí nào?
  → **Cấu hình:** `session.timeout.ms: 45000` (45s) + `heartbeat.interval.ms: 15000` (15s).
  → **Tại sao:** consumer có 1 thread nền gửi heartbeat định kỳ (`heartbeat.interval.ms`) cho
  broker; nếu broker không nhận được heartbeat nào trong `session.timeout.ms`, nó coi consumer
  đã chết và rebalance ngay. `session.timeout.ms` quá thấp ⇒ rebalance giả (do GC pause/network
  blip tạm thời) xảy ra thường xuyên, gây mất ổn định; quá cao ⇒ phát hiện consumer chết chậm,
  partition của nó "đóng băng" lâu trước khi được gán lại. `heartbeat.interval.ms` nên bằng
  khoảng 1/3 `session.timeout.ms` để có đủ vài nhịp heartbeat "hụt" trước khi bị coi là chết
  thật, tránh chỉ 1 lần mất gói tin đã kích hoạt rebalance.

- **Vấn đề:** yêu cầu "quan sát được" — biết consumer group đang active, và mỗi thread trong
  instance đang phụ trách partition nào — nhưng thông tin này không tự nhiên xuất hiện trong
  log mặc định của Spring Kafka.
  → **Cấu hình:** bean `ContainerCustomizer` gắn `ConsumerAwareRebalanceListener`, log ra
  group id, thread name, danh sách partition mỗi khi có `onPartitionsAssigned`.
  → **Tại sao:** rebalance xảy ra cả lúc startup lẫn lúc runtime (thêm/bớt instance, consumer
  bị coi là chết theo lý do ở trên) — log ngay tại thời điểm assign giúp xác nhận nhanh việc
  scale/failover có đang hoạt động đúng như kỳ vọng hay không, mà không cần chờ chạy lệnh CLI.

**Tự kiểm tra:**
- [ ] Với từng property trong 4 property tuning: vấn đề thực tế nào nó giải quyết, và điều gì xảy ra nếu thiếu hoặc cấu hình sai?
- [ ] Vì sao `max.poll.interval.ms` phải "lớn hơn thoải mái" so với `max-poll-records × thời gian xử lý 1 record trong trường hợp xấu nhất"? Vi phạm điều này thì consumer gặp chuyện gì?
- [ ] Vì sao `heartbeat.interval.ms` nên bằng khoảng 1/3 `session.timeout.ms`, không nên bằng hoặc lớn hơn?
- [ ] `ContainerCustomizer` log lại việc gán partition mỗi khi rebalance "kể cả lúc startup" — ngoài startup/shutdown, điều gì khác kích hoạt rebalance lúc runtime?
- [ ] Hiện tại `/actuator/health` và `/actuator/info` expose thông tin gì, và cần thêm gì (theo stretch goal của requirement) để xem consumer lag mà không cần SSH vào broker?

**Hands-on:**
```bash
curl localhost:8080/actuator/health
docker exec -it kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --describe --group payment-email-service
```
Đọc cột `LAG` theo từng partition và đối chiếu với log mà `containerCustomizer` in ra lúc startup.

---

## Session 7 — Chạy thử end-to-end (không đọc code, chỉ vận hành)

Đây là bước 8 trong `requirement.md` và mục "Not yet done" trong `summarize.md` — chỉ làm
sau khi Session 1–6 đã chắc, làm một lượt end-to-end.

1. `docker compose up -d`, start app từ đầu (bộ nhớ `processedPaymentIds` trống).
2. **Flow bình thường** — `POST /payments` một lần, xác nhận 1 log "Email sent" và có `ack`.
3. **Duplicate** — cùng `paymentId` gọi 2 lần, xác nhận chỉ 1 email được gửi + có log "Skipping duplicate".
4. **Poison pill (business logic)** — `simulateFailure: true`, xác nhận 3 lần retry + log DLT, và các message/partition khác vẫn chạy song song bình thường trong lúc đó.
5. **Poison pill (JSON hỏng)** — gửi raw string qua `kafka-console-producer`, xác nhận vào DLT mà app không bị crash.
6. **Kill giữa chừng lúc đang xử lý** — `POST /payments`, kill app (`kill -9`) càng nhanh càng tốt sau khi request trả về 202 nhưng trước khi thấy log "Email sent", restart, xác nhận message được gửi lại và xử lý đúng 1 lần (không phải 0 lần, không bị mất âm thầm).
7. Đối chiếu `kafka-consumer-groups.sh --describe` — lag phải về 0 sau một lượt chạy sạch.

**Ghi lại** (không cần format gì đặc biệt) điều gì thực sự xảy ra ở mỗi bước so với kỳ vọng —
khoảng cách đó thường chính là chất liệu tốt nhất để kể chuyện khi phỏng vấn.

---

## Sau khi xong tất cả session: Diễn tập phỏng vấn

Không mở file nào cả, trả lời 3 câu hỏi tổng quát trong `requirement.md` cho **toàn bộ hệ
thống**, không phải từng config riêng lẻ:
1. Mỗi thành phần giải quyết vấn đề gì?
2. Điều gì xảy ra nếu mỗi thành phần bị cấu hình sai hoặc bị thiếu?
3. Toàn bộ pipeline đảm bảo delivery guarantee gì, và vì sao điều đó bắt buộc idempotency
   phải nằm ở tầng application chứ không thể trông chờ vào Kafka?

Nếu session nào trong 1–6 vẫn còn mơ hồ, quay lại làm lại session đó trước khi tiếp tục.
