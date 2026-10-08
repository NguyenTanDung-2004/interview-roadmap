# Kafka Producer Publishing Logic Explained

## Overview Flow

```
User calls: publish(topic, key, event)
    ↓
[Send to Kafka - ASYNC]
    ↓
[Wait for Kafka response - 100ms]
    ↓
[Track in batchMap]
    ↓
[Schedule summary after 50ms]
    ↓
[Log batch summary]
```

---

## Detailed Breakdown

### 1. **publish() Method Called**
```java
public void publish(String topic, String key, T event) {
    int msgNum = messageCounter.incrementAndGet();  // Message #1, #2, #3, ...
    long sendTime = System.currentTimeMillis();     // Record when we send
    
    log.info("[BATCH DEMO] Sending message #{}...", msgNum);
```

**What happens:**
- Message gets a unique ID (msgNum)
- Record current time
- Log that we're sending message

---

### 2. **Send to Kafka (ASYNC with CompletableFuture)**
```java
CompletableFuture<SendResult<String, Object>> future = 
    kafkaTemplate.send(topic, key, event);
```

**What is CompletableFuture?**
- Represents a **Future computation** (will complete at some point)
- Instead of blocking and waiting, we specify what to do **WHEN** it completes
- Like a "callback" - **"Hey Kafka, send this message, and when you're done, tell me the result"**

**Timeline:**
```
Time 0ms:    publish() called
             kafkaTemplate.send() returns IMMEDIATELY (async)
             → Returns CompletableFuture object
             → Method continues (doesn't block)

Time 100ms:  Kafka responds
             → CompletableFuture completes with SendResult
             → Our callback (whenComplete) runs
```

---

### 3. **Handle Kafka Response (whenComplete Callback)**
```java
future.whenComplete((result, ex) -> {
    // This code runs ~100ms later when Kafka responds
    
    long deliveryTime = System.currentTimeMillis() - sendTime;  // ~100ms
    if (ex != null) {
        // Failed
        log.error("[BATCH DEMO] Message #{} FAILED after {}ms", msgNum, deliveryTime);
    } else {
        // Success - extract batch info
        int partition = result.getRecordMetadata().partition();
        long offset = result.getRecordMetadata().offset();
        int serializedSize = result.getRecordMetadata().serializedValueSize();
```

**What is whenComplete?**
- Tells Java: **"When this CompletableFuture completes, run this function"**
- `result`: The response from Kafka (metadata about where message was stored)
- `ex`: Exception if something went wrong

**RecordMetadata contains:**
```
Partition: 0, 1, 2, 3, 4, 5 (which shard the message went to)
Offset: 50, 51, 52, ... (position in that partition)
Timestamp: when broker received it
SerializedSize: 340 bytes (message size after serialization)
```

---

### 4. **Group Messages into Batches (batchKey)**
```java
long deliveryTimeBucket = (deliveryTime / 20) * 20;  // Round to nearest 20ms
String batchKey = partition + "_" + deliveryTimeBucket;
// Example: "1_100" = Partition 1, Delivered at 100ms bucket

synchronized (batchLock) {
    batchMap.computeIfAbsent(batchKey, k -> new BatchInfo(...))
            .addOffset(msgNum, offset, serializedSize);
```

**What happens:**
- Messages delivered at similar times → same batchKey
- Example:
  - Message #1 delivered after 105ms → bucket = 100 → "1_100"
  - Message #2 delivered after 108ms → bucket = 100 → "1_100" ✓ SAME BATCH
  - Message #3 delivered after 124ms → bucket = 120 → "1_120" ✗ DIFFERENT BATCH

**batchMap storage:**
```
batchMap = {
  "1_100": BatchInfo {
    offsets: {50: 340, 51: 340, 52: 340}  // offset -> size
  },
  "1_120": BatchInfo {
    offsets: {53: 340}
  }
}
```

---

### 5. **Schedule Batch Summary (ONCE per batch)**
```java
if (!batchLogScheduled.containsKey(batchKey)) {  // First message only!
    batchLogScheduled.put(batchKey, System.currentTimeMillis());
    CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS)
        .execute(() -> logBatchSummary(batchKey, partition));
}
```

**Why this is important:**
```
OLD CODE (BROKEN):
- Message #1 delivered → schedule logBatchSummary() ✓
- Message #2 delivered → schedule logBatchSummary() ✓
- Message #3 delivered → schedule logBatchSummary() ✓
- logBatchSummary() runs FIRST TIME → finds 1 message → logs "Count: 1"
- logBatchSummary() runs SECOND TIME → already logged, skips
- logBatchSummary() runs THIRD TIME → already logged, skips
RESULT: Only "Count: 1" shown ❌

NEW CODE (FIXED):
- Message #1 delivered → schedule logBatchSummary() ✓ (first time)
- Message #2 delivered → DON'T schedule (already scheduled) ✓
- Message #3 delivered → DON'T schedule (already scheduled) ✓
- logBatchSummary() runs → finds 3 messages → logs "Count: 3"
RESULT: "Count: 3" shown ✅
```

---

### 6. **Log Batch Summary (After 50ms delay)**
```java
private void logBatchSummary(String batchKey, int partition) {
    BatchInfo batch = batchMap.get(batchKey);
    if (batch != null && !batch.logged) {
        batch.logged = true;
        
        List<Long> offsets = new ArrayList<>(batch.offsets.keySet());
        Collections.sort(offsets);  // [50, 51, 52]
        
        int messageCount = batch.offsets.size();  // 3
        int totalSize = batch.offsets.values().stream()
            .mapToInt(s -> s).sum();  // 340+340+340 = 1020
        
        log.info("║ Messages in batch:      {}  ", messageCount);
        log.info("║ All offsets:            {}  ", offsets);
        log.info("║ Total batch size:       {} bytes", totalSize);
```

**Output:**
```
╔════════════════════════════════════════════════════════════╗
║              ✓ BATCH DELIVERY SUMMARY                      ║
║════════════════════════════════════════════════════════════║
║ Partition:              1                                  
║ Messages in batch:      3                                  
║ Offset range:           50 → 52                           
║ All offsets:            [50, 51, 52]                      
║ Total batch size:       1020 bytes                           
║ Avg message size:       340 bytes                            
╚════════════════════════════════════════════════════════════╝
```

---

## Full Timeline Example (3 messages)

```
TIME 0ms:
├─ Message #1: publish(topic, "same-key", event1)
│  └─ kafkaTemplate.send() returns immediately (async)
│     future = CompletableFuture<SendResult>
│
├─ Message #2: publish(topic, "same-key", event2)
│  └─ kafkaTemplate.send() returns immediately (async)
│     future = CompletableFuture<SendResult>
│
└─ Message #3: publish(topic, "same-key", event3)
   └─ kafkaTemplate.send() returns immediately (async)
      future = CompletableFuture<SendResult>

All 3 calls return INSTANTLY
Main thread can continue doing other stuff!

TIME ~100ms:
├─ Kafka batch ready (all 3 messages in one batch)
│
├─ Message #1: CompletableFuture completes
│  └─ whenComplete() callback runs
│  └─ addOffset(1, 50, 340)
│  └─ batchKey = "1_100"
│  └─ IF (batchLogScheduled doesn't have "1_100") THEN
│     └─ Schedule logBatchSummary("1_100", 1) after 50ms ✓
│
├─ Message #2: CompletableFuture completes
│  └─ whenComplete() callback runs
│  └─ addOffset(2, 51, 340)
│  └─ batchKey = "1_100"
│  └─ IF (batchLogScheduled doesn't have "1_100") THEN
│     └─ Skip (already scheduled) ✓
│
└─ Message #3: CompletableFuture completes
   └─ whenComplete() callback runs
   └─ addOffset(3, 52, 340)
   └─ batchKey = "1_100"
   └─ IF (batchLogScheduled doesn't have "1_100") THEN
      └─ Skip (already scheduled) ✓

TIME ~150ms (100ms + 50ms delay):
└─ logBatchSummary("1_100", 1) executes
   └─ batchMap["1_100"].offsets = {50: 340, 51: 340, 52: 340}
   └─ messageCount = 3
   └─ totalSize = 1020
   └─ Log: "Messages in batch: 3"
   └─ Log: "All offsets: [50, 51, 52]"
```

---

## Key Concepts

### CompletableFuture
- **Non-blocking**: send() returns immediately, doesn't wait for Kafka
- **Callback-based**: whenComplete() runs later when result arrives
- **Async**: Main thread can send multiple messages rapidly

### Batching
- Messages with **same key** → **same partition**
- Messages arriving within ~100ms → **grouped into one batch**
- Kafka sends one network request for all 3 messages (efficient!)

### Why batchLogScheduled map?
- **Prevents duplicate logging** of batch summary
- First message: schedules summary
- Second/third messages: skip scheduling (already queued)
- Result: logs once when all messages arrived

---

## Test Now!

```bash
curl "http://localhost:8080/test/batch-aggressive?count=10"
```

**Expected:**
```
✓ All 10 messages sent in 9ms

[BATCH DEMO] Message #1 DELIVERED after 105ms | Partition=1 | Offset=50 | ...
[BATCH DEMO] Message #2 DELIVERED after 105ms | Partition=1 | Offset=51 | ...
...
[BATCH DEMO] Message #10 DELIVERED after 105ms | Partition=1 | Offset=59 | ...

╔════════════════════════════════════════════════════════════╗
║              ✓ BATCH DELIVERY SUMMARY                      ║
║════════════════════════════════════════════════════════════║
║ Messages in batch:      10                                  
║ Offset range:           50 → 59                           
║ All offsets:            [50, 51, 52, 53, 54, 55, 56, 57, 58, 59]
╚════════════════════════════════════════════════════════════╝
```

All 10 messages batched together! 🎉
