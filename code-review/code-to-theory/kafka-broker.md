### Kafka Broker
- Chạy trên JVM: Mỗi Broker là một ứng dụng JVM (Java Virtual Machine) thực thi mã nguồn Scala/Java.
- Kafka Broker mặc định mở cổng TCP (9092) để lắng nghe và xử lý các Request, Response từ các client (Producer, Consumer, ...) hoặc từ các Broker khác trong cụm 
- Lưu trữ Log trên Disk: Broker không giữ dữ liệu trong RAM lâu dài mà ghi trực tiếp dữ liệu nhận được xuống hệ thống tệp (File System) thành các tệp tin dạng **Append-only Commit Log**

### Cách Kafka Broker thật sự xử lí với hàng nghìn message tới cùng lúc
- **Acceptor Thread**: nhận toàn bộ kết nối TCP từ các Producer gửi tới
- **Processor Threads (Network Threads)**: Đọc các byte dữ liệu nhị phân được gửi tới từ các Producer, và đóng gói thành các **Produce Request** và đẩy chúng vào bên trong **Request Queue**
- **Kafka Request Handler Threads**: Một tập hợp các **worker thread** sẽ lấy các **Produce Request** từ **Request Queue** ra để xử lí logic.

### Xử lí Batching
- Broker không ghi lẻ tẻ từng message 1 xuống đĩa.
+ Producer tự động gom các message nhỏ thành một **Batch** rồi sau đó mới gửi qua mạng (Gom các message cùng 1 **Partition** của cùng **Topic**)
+ Broker nhận toàn bộ **Batch** này dưới dạng **raw byte** duy nhất. Cả quá trình nhận, truyền qua Queue và ghi đĩa đều giữ nguyên Batch này mà **không giải nén hay parse ra từng message** bên trong.
- 2 cấu hình **kafka.producer.max-size**, **kafka.producer.linger-ms**

### Ghi dữ liệu cực nhanh vào OS Page Cache
1. Dữ liệu không được ghi trực tiếp xuống ổ đĩa (ssh/hdd) vì I/O rất chậm.
2. Broker sẽ ghi dữ liệu vào **RAM Page Cache** của OS
3. Sau khi dữ liệu được ghi thành công vào **RAM Page Cache**, Broker ngay lập tức phản hồi về cho Producer.
4. Việc data thực sự được ghi từ RAM xuống ổ đĩa cứng **Flush to Disk** sẽ do Kernel của OS chịu trách nhiệm chạy ngầm hoặc theo chu kì cấu hình **log.flush.interval.messages**

### Đẩy nhanh vào đĩa nhờ Ghi nối tiếp (Sequential I/O)
- Mỗi partition sẽ có một thư mục riêng để lưu trữ dữ liệu
- Với mỗi message, Broker chỉ cần **append-only** (ghi nối tiếp vào file segment .log cuối cùng)
- **Sequential Write** (Ghi nối tiếp) cho tốc độ nhanh gấp hàng trăm lần so với **Random Write** (như trên các RDBMS)

-- Thiết lập POC xem được 1 **Produce Request** từ Producer gửi lên Kafka