### ZooKeeper (Không còn hỗ trợ kể từ Kafka 3.0+)
- là một **distributed coordination service**
+ Cluster management: track brokers nào còn sống hay chết.
+ Leader election: chọn broker nào là controller
+ Configuration management: lưu trữ metadata của broker và topic
+ Synchronized State: duy trì tính consistent giữa broker của cluster
=> Vì là một service độc lập, vì vậy cần phải duy trì cả 2.

### KRaft (Kafka Raft)
- được xây dựng để thay thế Zookeeper
- Các Brokers sẽ tự lưu thông tin của chúng.
- ...

