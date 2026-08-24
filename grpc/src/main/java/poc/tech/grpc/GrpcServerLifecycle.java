//package poc.tech.grpc;
//
//import io.grpc.Server;
//import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.SmartLifecycle;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//
//@Component
//class GrpcServerLifecycle implements SmartLifecycle {
//
//    private final GrpcServerService grpcServerService;
//    private final int grpcPort;
//    private volatile boolean running;
//    private Server server;
//    private Thread awaitThread;
//
//    GrpcServerLifecycle(GrpcServerService grpcServerService,
//                        @Value("${grpc.server.port:9090}") int grpcPort) {
//        this.grpcServerService = grpcServerService;
//        this.grpcPort = grpcPort;
//    }
//
//    @Override
//    public void start() {
//        if (running) {
//            return;
//        }
//        try {
//            this.server = NettyServerBuilder.forPort(grpcPort)
//                    .addService(grpcServerService)
//                    .build()
//                    .start();
//
//            // Keep one non-daemon thread blocked on the server so the JVM does not exit.
//            this.awaitThread = new Thread(() -> {
//                try {
//                    server.awaitTermination();
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
//            }, "grpc-server-await");
//            this.awaitThread.setDaemon(false);
//            this.awaitThread.start();
//
//            this.running = true;
//        } catch (IOException ex) {
//            throw new IllegalStateException("Failed to start gRPC server", ex);
//        }
//    }
//
//    @Override
//    public void stop() {
//        if (server == null) {
//            running = false;
//            return;
//        }
//        server.shutdown();
//        try {
//            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
//                server.shutdownNow();
//            }
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//            server.shutdownNow();
//        } finally {
//            if (awaitThread != null) {
//                awaitThread.interrupt();
//                awaitThread = null;
//            }
//            running = false;
//        }
//    }
//
//    @Override
//    public boolean isRunning() {
//        return running;
//    }
//
//    @Override
//    public boolean isAutoStartup() {
//        return true;
//    }
//
//    @Override
//    public int getPhase() {
//        return Integer.MAX_VALUE;
//    }
//}
//
