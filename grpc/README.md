# Spring gRPC POC

This module is a small gRPC server POC built with Spring Boot and Java.

## 1) Project structure

```text
grpc/
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ proto/
│  │  │  └─ hello.proto
│  │  ├─ java/poc/tech/grpc/
│  │  │  ├─ GrpcApplication.java
│  │  │  ├─ GrpcServerService.java
│  │  │  ├─ GrpcServerLifecycle.java
│  │  │  └─ ServletInitializer.java
│  │  └─ resources/
│  │     └─ application.yaml
│  └─ test/java/poc/tech/grpc/
│     ├─ GrpcApplicationTests.java
│     └─ GrpcServerServiceIntegrationTests.java
└─ target/ (generated + compiled output)
```

## 2) What each important file does

- `pom.xml`: dependencies and protobuf/gRPC code generation plugin setup.
- `src/main/proto/hello.proto`: API contract (service + messages). This is the source of truth for RPC definitions.
- `src/main/java/poc/tech/grpc/GrpcServerService.java`: server implementation for all RPC methods.
- `src/main/java/poc/tech/grpc/GrpcServerLifecycle.java`: starts/stops the Netty gRPC server in Spring lifecycle.
- `src/main/resources/application.yaml`: runtime config (`grpc.server.port`, app name).
- `src/test/java/poc/tech/grpc/GrpcServerServiceIntegrationTests.java`: integration tests for gRPC calls.

## 3) gRPC methods in this POC

Defined in `src/main/proto/hello.proto`:

- `SayHello`: unary request/response.
- `StreamHello`: one request, many responses (server streaming).
- `ChatHello`: many requests and many responses on one RPC (bidirectional streaming).

### Benefit of each function in `GrpcServerService`

- `sayHello(...)`
  - Best for simple request/response operations.
  - Easy to debug and a good default pattern for CRUD-style RPCs.
  - Lowest complexity for client and server code.

- `streamHello(...)`
  - Best when one request should return many items progressively.
  - Client can start processing early instead of waiting for a full batch.
  - Useful for notifications, chunked data, and long-running result feeds.

- `chatHello(...)`
  - Best for interactive, back-and-forth communication on one connection.
  - Reduces overhead compared with opening many unary calls.
  - Useful for chat, live collaboration, real-time command/response streams.

- `onNext(...)` inside `chatHello(...)`
  - Handles each incoming client message immediately.
  - Enables low-latency response per message.

- `onCompleted(...)` inside `chatHello(...)`
  - Closes server response stream cleanly after client finishes sending.
  - Important for releasing resources and signaling stream completion.
  - In practice, this is reached when the client completes the gRPC stream (and can also happen if the underlying connection is terminated).

- `onError(...)` inside `chatHello(...)`
  - Captures stream failures for diagnostics/logging.
  - Prevents silent failures and helps operations troubleshooting.

## 4) How request flow works

Important note:

- gRPC server in this project is different from a Spring Boot HTTP server.
- The gRPC server is started by `GrpcServerLifecycle` using Netty and listens on `grpc.server.port`.
- If you later add a Spring Boot web server (`spring-boot-starter-web`), that HTTP server would use a different port (`server.port`) and serve REST/HTTP endpoints.

1. Client sends request to method in `Simple` service.
2. gRPC server routes to `GrpcServerService` method implementation.
3. Service builds `HelloReply` and writes it via `StreamObserver`.
4. For streaming methods, multiple `onNext(...)` calls are sent before `onCompleted()`.

## 5) Run and test

Run tests:

```powershell
.\mvnw.cmd clean test
```

Run app:

```powershell
.\mvnw.cmd spring-boot:run
```

## 6) Try APIs quickly

Unary:

```powershell
grpcurl -d '{"name":"Hi"}' -plaintext localhost:9090 Simple.SayHello
```

Server streaming:

```powershell
grpcurl -d '{"name":"Hi"}' -plaintext localhost:9090 Simple.StreamHello
```

Bidirectional streaming (Postman):

1. Import `src/main/proto/hello.proto`.
2. Use URL `127.0.0.1:9090`.
3. Set TLS/SSL to OFF (plaintext).
4. Choose `Simple/ChatHello`.
5. Send multiple payloads in one stream, for example:

```json
{"name":"A"}
{"name":"B"}
{"name":"C"}
```

## 7) Maintenance guide

- Change API in `.proto` first, then regenerate by running Maven build.
- Never reuse old protobuf field numbers for new meaning.
- Keep transport logic in `GrpcServerService`; move business logic to separate service classes if complexity grows.
- Add/update integration tests when adding RPC methods.
- If port `9090` is busy, run with another port:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--grpc.server.port=9091"
```

If `grpcurl` is not installed, use Postman gRPC, BloomRPC, or another gRPC client.

