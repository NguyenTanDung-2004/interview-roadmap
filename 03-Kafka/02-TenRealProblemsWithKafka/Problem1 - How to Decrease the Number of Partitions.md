### Scenario
```
Timeline:
├─ Day 1: Created topic "orders" with 100 partitions
│         Reason: Expected high volume
│
├─ Day 30: Actual traffic is only 10K msg/sec
│          100 partitions is overkill
│          Wasting resources (broker CPU, memory, disk)
│
└─ Goal: Reduce to 10 partitions to save resources
```

### Why It Happens
```
Indicators:
├─ Check broker CPU/memory usage
│  └─ High resource usage despite low throughput?
│
├─ Check partition distribution
│  └─ Many partitions idle/empty?
│
├─ Monitor throughput per partition
│  └─ Each partition handling very little data?
│
└─ Tool: Check with Kafka tools
   $ kafka-topics.sh --describe --topic orders
   └─ See: 100 partitions, but low activity
```

### How to Find it
```
Indicators:
├─ Check broker CPU/memory usage
│  └─ High resource usage despite low throughput?
│
├─ Check partition distribution
│  └─ Many partitions idle/empty?
│
├─ Monitor throughput per partition
│  └─ Each partition handling very little data?
│
└─ Tool: Check with Kafka tools
   $ kafka-topics.sh --describe --topic orders
   └─ See: 100 partitions, but low activity
```