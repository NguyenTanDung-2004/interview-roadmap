### 1. What is Kafka and why do we use it?
Kafka is a distributed event streaming platform. It is used to process large volumes of real-time data. It is designed for **High Throughput**, **Scalability**, **Fault Tolerance**. Kafka is useful in Event Drivent Architecture and when we communicate with independent services.

### 2. What is kafka broker, and cluster?
Broker is a kafka server where store and mange messages via partitions and topics. It allows producers to publish message and then serves those messages to consumers. Multiple brokers form a cluster that allows kafka to scale and provide fault tolerance.

### 3. What is topic, partition and why do we need to use partition?
A Topic is a logical category to store messages. A topic has many partitions used to order the messages. Using multiple partitions across multiple brokers allows kafka consumers process the messages in parallel.
Example: We have 3 partition in each topic => Upto 3 consumers can process the message in the same consumer-group simultaneously.

### 4. How does Kafka determine which partition a message goes to?
A message published by producer contains key and value. Kafka hashes this key and use this value to determine what partition a message should go to. If the order of messages is important we need to choose the key carefully. Typically, we use the entityId as the key and all the messages related to the same entity will go to the same partition.