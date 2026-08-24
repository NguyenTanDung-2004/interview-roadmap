# Kafka Learning Roadmap

A step-by-step guide to mastering Apache Kafka from core concepts to production ecosystems[cite: 1].

---

## Stage 1: The Core Mental Model (Foundations)

Understand the 6 foundational concepts before writing code[cite: 1]:

1. **Events (Messages):** Key-value pairs with optional timestamps/headers representing system state changes[cite: 1].
2. **Topics:** Named streams or logical folders where events are stored[cite: 1].
3. **Partitions & Offsets:** 
   * **Partition:** A topic's physical log split across brokers for scalability[cite: 1].
   * **Offset:** A sequential ID tracking read/write positions within a partition[cite: 1].
4. **Producers:** Applications writing events into Kafka topics[cite: 1].
5. **Consumers & Consumer Groups:** Applications reading events; groups share partition reads for parallel processing[cite: 1].
6. **Brokers & Clusters:** Individual Kafka servers (brokers) forming a coordinated cluster[cite: 1].

---

## Stage 2: Hands-On Basics (CLI & Local Setup)

Get familiar with local setup and command-line execution[cite: 1]:

* **Setup:** Run Kafka locally using **KRaft mode** via Docker Compose[cite: 1].
* **CLI Operations:** Practice key administrative and debugging commands[cite: 1]:
  * Create, describe, and delete topics[cite: 1].
  * Send messages using `kafka-console-producer`[cite: 1].
  * Read events using `kafka-console-consumer`[cite: 1].
  * Monitor consumer lag and group health via `kafka-consumer-groups`[cite: 1].

---

## Stage 3: Programmatic Producers & Consumers

Integrate Kafka into application code (Python, Java, Go, etc.)[cite: 1]:

* **Serialization:** Format payloads using JSON, Strings, or Avro/Protobuf with Schema Registry[cite: 1].
* **Delivery Semantics:**
  * **Producer ACKs:** Learn `acks=0`, `acks=1`, and `acks=all` trade-offs[cite: 1].
  * **Consumer Guarantees:** Configure *at-least-once*, *at-most-once*, or *exactly-once* processing[cite: 1].
* **Partitioning Keys:** Use key routing to enforce strict ordering for target entities[cite: 1].

---

## Stage 4: Advanced Kafka Ecosystem & Operations

Scale system integrations and real-time processing[cite: 1]:

* **Kafka Connect:** Integrate databases (CDC), storage buckets (S3), and search indexes without custom code[cite: 1].
* **Stream Processing:** Build real-time transformation pipelines using **Kafka Streams** or **Apache Flink**[cite: 1].
* **Cluster Operations:** Manage partition rebalancing, replication factors, and system metrics[cite: 1].