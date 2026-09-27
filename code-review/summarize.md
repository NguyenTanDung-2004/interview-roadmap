# Kafka Payment Consumer — Implementation Summary

High-level summary of the work done against `requirement.md` in `spring-boot-base-poc`.

## What was built

- **Producer:** `POST /payments` (`PaymentController` → `PaymentService`) publishes a
  `CreatePaymentEvent` (`paymentId`, `amount`, `currency`, `customerEmail`) to the
  `payment-created` topic, keyed by `paymentId`. The request also accepts an optional
  `paymentId` and a `simulateFailure` flag purely for manual testing (resend the same
  `paymentId` to test idempotency, or set the flag to force a poison-pill/DLT scenario).

- **Topic provisioning:** `KafkaTopicConfig` declares `NewTopic` beans for `payment-created`
  (6 partitions, replication factor 1) and `payment-created.DLT`, created automatically on
  startup via Spring Boot's `KafkaAdmin`.

- **Consumer:** `CreatePaymentConsumer` — `@KafkaListener` on `payment-created`, consumer
  group `payment-email-service`, `concurrency = 3`. Uses manual acknowledgment
  (`enable-auto-commit: false`, `ack-mode: manual_immediate`) and only calls
  `ack.acknowledge()` after the confirmation email is "sent" (logged) and the payment is
  recorded — this is what gives crash-safety (no ack before restart ⇒ redelivered).

- **Idempotency:** `ProcessedPaymentRepository` (in-memory, `ConcurrentHashMap`-backed)
  tracks processed `paymentId`s; the listener skips and acks immediately on a duplicate
  instead of re-sending the email.

- **Poison-pill / error handling:** `KafkaErrorHandlingConfig` wires a `DefaultErrorHandler`
  with a `FixedBackOff` (3 retries, 1s apart) and a `DeadLetterPublishingRecoverer` that
  republishes exhausted records to `payment-created.DLT`, with `commitRecovered(true)` so
  manual-ack mode still advances past dead-lettered offsets. `PaymentDeadLetterListener`
  consumes the DLT topic (via its own plain-`String` container factory, so it can't fail
  deserializing a payload that landed there *because* it wasn't valid JSON) and just logs
  it. Deserializers are wrapped in `ErrorHandlingDeserializer` so malformed-JSON poison
  pills go through the same retry/DLT path as business-logic exceptions, rather than
  crashing the poll loop.

- **Tuning / observability:** `max.poll.records`, `max.poll.interval.ms`,
  `session.timeout.ms`, and `heartbeat.interval.ms` are set in `application.yaml` with
  comments explaining what each protects against. A `ContainerCustomizer` bean logs the
  consumer group and assigned partitions per listener thread on every rebalance
  (including startup).

## Notable fix along the way

A `git stash` from earlier work in this repo was restored mid-task, but it didn't include
the in-flight `pom.xml`/`application.yaml` edits — those had reverted to their initial
state (no Kafka/lombok/validation dependencies, no Kafka config at all). Both were rebuilt
from scratch as part of this implementation.

## Deliberate simplifications (called out, not hidden)

- No real database — idempotency and topic config use in-memory state / Spring beans, per
  the spec ("H2 or in-memory store is fine").
- No exactly-once/Kafka transactions — out of scope per spec; the whole design is
  at-least-once, which is exactly why the idempotency check exists.
- Removed an earlier `SayHello`/generic `CustomKafkaConsumer` scaffold from a prior
  session — its catch-and-log pattern would have swallowed exceptions before the
  container's error handler ever saw them, silently disabling the retry/DLT path.

## Not yet done

- No automated end-to-end run against the `docker-compose.yml` Kafka broker in this repo
  yet (normal flow / duplicate / poison-pill / kill-mid-processing manual test pass from
  the spec's suggested build order, step 8).
- No embedded-Kafka (`spring-kafka-test`) integration test written yet, though the
  dependency is already on the classpath.
