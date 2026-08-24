![](/images/Short,long%20polling,%20SSE,%20WebSocket.png)


A HTTP can not initiate a connection to a browser. As a result, the web browser is an initiator. **What should we do next to get real-time update from the HTTP Server?**

Both the web browser and the HTTP Server could be responsible for this:
- **Web browsers do the heavy lifting**: 
    + Short Polling: the browser will retry until it gets the latest data. (Background status check, Batch Processing status check, ...)
    + Long Polling: the HTTP server does not return results until new data has arrived.
- **HTTP server and Web browser cooperate**:
    + WebSocket/SSE (Server-sent event): HTTP server can directly send data to web browser when a connection is established. 
    + Difference:
        + SSE (Server-sent event): is uni-directional, so the web browser can not send a new request to the server. (Realtime feeds, System notification,...)
        + WebSocket: is fully-duplex, so the browser can keep sending new requests. (interactive apps, chat application,...)


**Why do we need Long Polling?**

Long polling remains a critical design pattern in distributed systems. It is essential specifically in environments where **updates occur unpredictably**, **events are rare**, or **legacy network infratruscture** prevents persistent statefull connections like WebSocket, SSE
1. **Asynchronous Job & Task Progress Tracking**
    - **Scenario:** triggering heavy backedn tasks like generating a PDF report.
    - **Why Long polling:** the server holds the request until the background worker emits "completed" status, the server return results immediately upon completion without hammering the database with frequent status queries.
2. **Distributed Message Queues & Microservice Pull Models**
    - **Scenario:** Customer services fetch message from distributed queues (e.g, AWS SQS, or Kafka HTTP gateways)
    - **Why Long polling:** Consumers send request to Queues, and these hold it until a new message enters. This reduce empty response to nearly zero and drastically lowers API billings and CPU polling overhead.