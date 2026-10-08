## Consumer-group
Each instance acts as a consumer, they need to have **groupId**. This configuration is used to resolve the problems belo:
- If we don't use this configuration, Kafka will treat those instances as independent. **So one message will be processed by them.**
- Kafka has many partitions, each partition is only assinged to one instance in a consumer-group at a specific point in time. 
  + Kafka stores offset by consumer-group. If we don't use groupId, Kafka will not known which messages have been processed.
  + Example: Instance A is listening to the Parition1, Partition2, ..., when InstanceA encounters a failure, using groupId allows kafka to reassign all partitions that Istance A was processing to another instance, thereby preventing messages from being permanently hold.

Typically, instances with the same consumer use the same code logic. There are several cases where these instances use the different code logic like **Canary Deployment**
- **Canary Deployment:** All the instances in the system are using the **version1** for the code logic. Now, we would like to test on the version2. Therefore, we will deploy the version2 to one pod and keep the current version on the remaining pods.
- The last partition will be designated as the **Canary Partition**. Kafka Libaries supports getting the number of partitions at the cluster level. When a message needs to be sent to the **Canary Partition**, the producer will consider pushing that message into this final partition. (There are various strategies to consider regarding which messages are routed to the **Canary Partition**—one of them is probability-based.)

