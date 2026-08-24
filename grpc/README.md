# Spring gRPC POC

This module is a minimal gRPC server POC based on the Spring gRPC getting-started guide.

## What is included

- `Simple` gRPC service in `src/main/proto/hello.proto`
- Spring gRPC server implementation in `src/main/java/poc/tech/grpc/GrpcServerService.java`
- Unary RPC: `SayHello`
- Server-streaming RPC: `StreamHello`
- Integration test calling the running gRPC server

## Quick start

1. Build and run tests:

```powershell
.\mvnw.cmd test
```

2. Run the app:

```powershell
.\mvnw.cmd spring-boot:run
```

3. Call unary method with grpcurl:

```powershell
grpcurl -d '{"name":"Hi"}' -plaintext localhost:9090 Simple.SayHello
```

4. Call server-stream method with grpcurl:

```powershell
grpcurl -d '{"name":"Hi"}' -plaintext localhost:9090 Simple.StreamHello
```

If `grpcurl` is not installed, use BloomRPC, Postman gRPC, or another gRPC client.

