1. Kafka partition is used to order the messages.

2. A kafka cluster has many brokers. The partitions of a topic are distributed across the brokers that allows kafka to scale horizontally.

3. Setting replication factor for a topic determines how many copies of each partition are stored across different brokers. 
Example: We set 2 replicas, 4 partitions for topicA. And there are 3 brokers in the system. 
=> Up to 4 consumers can process messages in TopicA simuntaneously. 
=> Each broker has 8 copies of data - each partition <=> 2 partitions. (Replica - FaultTolerance)

4. Replica, Partition configuration are setup at Topic level.