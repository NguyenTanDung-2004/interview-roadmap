### Problem Typ1: Asynchronous Long-Running Operations (High-Level)
User tốn nhiều thời gian để thực hiện một giao dịch
- User tạo transaction: 
+ call service A
+ call service B 
+ call service C
...
Sau đó mới có thể lấy được response.

Về mặt resource:
- Việc giữa một connection quá lâu, trong trường hợp có khoảng vài trăm ngừoi cùng chạy đồng thời => làm cho tình trạng server trở nên cạn kiệt tài nguyên (Resource Exhaustion)

Về mặt Data:
- Việc giao tiếp tuần tự như vậy làm việc đảm bảo data consistency trở nên khó khăn. Khi một luồng bị lỗi, rất khó để rollback.

### Approach to resolve this problem
## 1. Threading 
- Luồng chính
    - Xử lí luồng cốt lõi và lưu xuống database
    - Tạo các thread để xử lí cho các heavy-tasks
    - trả về response ngay lập tức. 
- Các luồng phụ 
    - email sent (run seperately)
    - Payment thread (run seperately)
    ...

Pros: trả về response ngay lập tức. Cons: khó rollback các data, server crash: toàn bộ pending task lost, nhiều user thực hiện thì các tốn rất nhiều tài nguyên cho việc duy trì các thread này.

## 2. Message Queue (Rabbit MQ, Active MQ)
Pros: trả về response ngay lập tức.
Cons: message bị xoá sau khi xử lí xong => Các consumer không truy cập được các message cũ
                                        => không thể replay lại các message cũ để debug

## 3. Kafka
Pros: 
- trả về response ngay lập tức, có thể cấu hình nhiều consumer, consumer mới có thể đọc được các message cũ.
- lưu trữ lịch sử của các message (7, 30 or configurable)
Cons: 
- vận hành phức tạp hơn
- Higher latency than message queue - (50-100ms vs 5-10s)


### Tại sao Kafka lại High Latency hơn Message Queue
Kafka: batching. Tuỳ vào cấu hình ACK để sync up data tới các replica khác.
Message Queue: gửi message đơn lẻ. Vẫn có batching (nhưng khi gom đủ số lượng n thì vẫn sẽ thiết lập n lần call gửi riêng lẻ). Việc sync up data tới các minnor brokers là nằm trong background jobs.
