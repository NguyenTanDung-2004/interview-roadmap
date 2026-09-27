# Kafka Payment Consumer — Implementation Spec

## Scenario

A payments company publishes `payment-created` events to a Kafka topic whenever
a payment is made. This service consumes those events and sends a confirmation
email. The implementation must handle scaling, crash-safety, duplicate
prevention, poison-pill messages, and observability.

## Tech Stack

- Java 21
- Spring Boot 3.x
- Spring Kafka
- Maven or Gradle (either is fine)
- H2 or in-memory store is fine for the "processed payments" table (no real DB needed)
- Local Kafka via Docker Compose for testing (docker-compose.yml)

## Topic

- Name: read application.yaml
- Partitions: 6
- Replication factor: 1 (local/single-broker dev setup)

## Functional Requirements

### 1. Producer (for testing)

- REST endpoint `POST /payments` that accepts a JSON body and publishes a
  `CreatePaymentEvent` to the `payment-created` topic.
- `CreatePaymentEvent` fields: `paymentId` (String, UUID), `amount` (BigDecimal),
  `currency` (String), `customerEmail` (String).
- Use `paymentId` as the Kafka message key (ensures same payment always lands
  on the same partition).

### 2. Consumer — basic setup

- `@KafkaListener` on topic `payment-created`, consumer group `payment-email-service`.
- `concurrency = 3` (3 threads within this instance; note in code comments why
  this is capped by partition count).
- On receipt, call a `sendConfirmationEmail(event)` method (can just log
  "Email sent to X for payment Y" — no real email integration needed).

### 3. Manual acknowledgment (crash-safety)

- Disable auto-commit.
- Set `ack-mode: MANUAL_IMMEDIATE`.
- Listener method signature includes `Acknowledgment ack`.
- Only call `ack.acknowledge()` after processing succeeds.

### 4. Idempotency (no duplicate emails)

- In-memory `Set<String>` or a simple repository (`ProcessedPaymentRepository`)
  tracking processed `paymentId`s.
- Before sending the email, check if `paymentId` was already processed.
  If yes, skip sending and just acknowledge.
- If no, send the email, record the `paymentId` as processed, then acknowledge.

### 5. Poison pill / error handling

- Configure a `DefaultErrorHandler` bean with:
  - `FixedBackOff` — retry 3 times, 1 second apart.
  - `DeadLetterPublishingRecoverer` — after retries are exhausted, publish the
    failed message to a dead-letter topic (`payment-created.DLT`).
- Add a `@KafkaListener` on `payment-created.DLT` that just logs the failed
  message (simulating "someone will investigate this").
- Include a way to simulate a poison pill (e.g., a malformed JSON payload sent
  directly to the topic, or a flag in the event that forces an exception).

### 6. Consumer tuning / observability

- Configure and be able to explain:
  - `max.poll.records`
  - `max.poll.interval.ms`
  - `session.timeout.ms`
  - `heartbeat.interval.ms`
- Add an actuator endpoint or simple log line on startup that prints the
  active consumer group and assigned partitions per listener thread.
- (Stretch) Expose consumer lag via Micrometer/Prometheus or document how to
  check it via `kafka-consumer-groups.sh --describe --group payment-email-service`.

## Non-Functional / Learning Goals

For each piece implemented, be able to answer in an interview:

- What problem does this config solve?
- What happens if it's misconfigured or missing?
- What delivery guarantee does the overall setup provide (at-least-once), and
  why does that require idempotency at the application level?

## Suggested Build Order

1. Docker Compose with Kafka + Zookeeper (or KRaft mode), create the
   `payment-created` topic with 6 partitions.
2. Producer REST endpoint + `CreatePaymentEvent`.
3. Basic consumer with `concurrency = 3`, just logging.
4. Switch to manual ack.
5. Add idempotency check.
6. Add error handler + DLT + DLT listener.
7. Add consumer tuning properties + a way to inspect partition assignment.
8. Manual test pass: normal flow, duplicate message, poison pill, killing the
   app mid-processing to confirm no message loss.

## Out of Scope

- Real email sending (log only).
- Exactly-once semantics / Kafka transactions (mention as a follow-up topic,
  not required for this implementation).
- Authentication/authorization on the REST endpoint.
- Production-grade schema registry / Avro (JSON is fine for this exercise).