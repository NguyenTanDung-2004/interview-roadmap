### Leader - Replica Message Flow

**Context**: hệ thống có 4 **Producers**, 3 **Brokers**

* step1: Các **Producers** sẽ push message vào trong **Leader Broker**
- Store message vào trong **OS Page Cache (RAM)**
- Trả Response về **Producer**

* Step2: Các Replica **(Broker2, Broker3)** sẽ chạy **background job** 
- để fetch và store các messages mới.
- sau khi hoàn tất thì **Leader Broker** sẽ đánh dấu là **replicated**


Configuration: **acks**
* First Scenario: 0
- Producer send message
- Broker receives (or not)
- Producer: no need to wait, gửi message tiếp theo 
=> best throughput
x: Không đảm bảo message đã được nhận hay chưa.

* Second Scenario: **1** (Leader Ackknowledgment)
- Producer send message
- Leader Broker put to Ram Page Cache
- Producer sends next message.
=> Good throughput
x: Data loss if leader crashes trong quá trình lưu xuống đĩa và sync up với các replica khác

* Finally: **all**
- At lease 2 replications
- Producer sends message to **Leader Broker**
- Leader Broker sync data tới các replicas khác
- Khi các Broker khác nhận được message thì mới response về Producer và ngược lại
