# Comprehensive Guide & Cheatsheet: Network Protocols, gRPC, REST, and WebSocket

---

## 1. Core Protocol Evolution: HTTP/1.1 vs HTTP/2

### HTTP/1.1
* **Format:** Plaintext (Text-based string payloads like JSON/XML).
* **Execution Model:** Strict single Request–Response cycle per active stream.
* **Limitations:**
  * **Head-of-Line (HOL) Blocking:** Each TCP connection handles only 1 request at a time. Other requests must queue up; if 1 request is slow, all subsequent requests are blocked.
  * **Header Overhead:** Uncompressed text headers are sent repeatedly with every single request, wasting bandwidth.

### HTTP/2
* **Multiplexing:** Allows multiple bi-directional requests and responses to stream concurrently over a **single TCP connection** (eliminates HOL blocking).
* **Binary Framing:** Breaks data down into binary frames instead of plain text, optimizing machine processing speed and accuracy.
* **Header Compression (HPACK):** Uses an indexing table and Huffman coding to strip repetitive header strings across requests.

---

## 2. Technical Comparison Matrix

| Feature / Metric | RESTful API | gRPC | WebSocket |
| :--- | :--- | :--- | :--- |
| **Transport Protocol** | HTTP/1.1 (or HTTP/2) | **HTTP/2** (Strict) | HTTP/1.1 Upgrade $\rightarrow$ TCP |
| **Payload Format** | Plaintext (JSON / XML) | **Binary (Protocol Buffers)** | Text or Binary |
| **Contract / Schema** | Optional (OpenAPI/Swagger) | **Strict (`.proto` file)** | None (Custom implementation) |
| **Primary Target** | Client-to-Server (Web/Mobile) | **Server-to-Server / IoT** | Client-to-Server (Web Realtime) |
| **Browser Support** | Native (Supported everywhere) | Requires `gRPC-Web` Proxy | Native (Supported everywhere) |
| **Streaming Support** | Unary (SSE limited) | 4-Way (Unary, Server, Client, Bi-di) | Bi-directional streaming |

---

## 3. Deep-Dive: How gRPC Serializes and Compresses Data

* **Payload Compression (Protocol Buffers vs. JSON):** Instead of transmitting text keys (field names), Protobuf uses a shared `.proto` schema (a "dictionary") stored on both client and server. It replaces text key names with integer **Tag IDs** and encodes string/numeric values into binary.
  * *JSON Example (`{"name":"Pen","price":10}`):* Transmitted as plain ASCII text $\rightarrow$ **25 bytes**.
  * *Protobuf Binary Example (`0A 03 50 65 6E 10 0A`):* `0A` (Tag 1 + String type), `03` (length 3), `50 65 6E` ("Pen"), `10` (Tag 2 + Int type), `0A` (value 10) $\rightarrow$ **6 bytes**.
  * *Impact:* **76% reduction in payload size.**

* **Header Compression (HPACK Algorithm):**
  * *HTTP/1.1 (REST):* Transmits raw text headers repeatedly for every call (e.g., `Authorization: Bearer eyJhbGci...` at 500 bytes per request $\rightarrow$ **~500 KB transferred** for 1,000 requests).
  * *HTTP/2 (gRPC):* Request #1 sends the full header. Server caches it at Index `#62`. Subsequent requests send only the 1-byte Index pointer `#62` $\rightarrow$ **>99% header bandwidth savings**.

* **Network Throughput via Multiplexing:**
  * *Scenario:* Sending 100 concurrent API requests over a network with 100ms latency.
  * *HTTP/1.1 (Browser limited to 6 TCP connections):* Processes requests in batches of 6 $\rightarrow$ **~1,700ms total time**.
  * *HTTP/2 (gRPC multiplexing over 1 TCP connection):* Streams all 100 requests in parallel $\rightarrow$ **~100ms total time** (**17× faster throughput**).

---

## 4. Use Cases & Architectural Guidance

### When to use gRPC (Server-to-Server)
1. **Internal Microservices:** Ultra-low latency, high-throughput communication between backend services inside Kubernetes or Docker clusters.
2. **Polyglot Environments:** Auto-generating strongly typed client/server code stubs across different languages (e.g., Go backend communicating with Python AI models and Java services).
3. **High-Throughput Streaming:** Real-time data feeds like log aggregation, financial order books, or audio/video processing pipelines.
4. **IoT & Mobile Applications:** Minimizes battery drain and bandwidth usage on resource-constrained devices over weak networks.

### When to use WebSocket (Client-to-Server)
* **Web Frontend Realtime:** Live chat apps, interactive web dashboards, or notification engines where the client is a Web Browser (React, Vue, vanilla JS) talking directly to a backend.

### Why WebSocket is NOT Ideal for Server-to-Server Communication
* **No Built-in Schema / Type Safety:** WebSockets transfer unstructured text or raw binary frames without a standardized API contract—you must build custom routing and parsing logic from scratch.
* **No Native Multiplexing:** Handling different API endpoints or events over a single WebSocket connection requires writing application-level multiplexing logic.
* **No Automatic Code Generation:** Unlike gRPC’s `protoc` compiler, WebSockets do not auto-generate client stubs across programming languages, increasing boilerplate and maintenance overhead.

---

## 5. Decision Rules of Thumb

* **Use REST:** For public APIs, third-party integrations, or standard Browser-to-Backend CRUD operations.
* **Use WebSocket:** For real-time, bi-directional Browser-to-Backend features (Chat, Live Notifications).
* **Use gRPC:** For high-performance backend microservice communication, heavy data streaming, or resource-constrained IoT/Mobile integrations.

## 6. POC (branchName: poc/gRPC)

