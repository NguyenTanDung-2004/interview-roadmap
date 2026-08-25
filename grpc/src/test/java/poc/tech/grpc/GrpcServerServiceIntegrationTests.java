package poc.tech.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import poc.tech.grpc.proto.HelloReply;
import poc.tech.grpc.proto.HelloRequest;
import poc.tech.grpc.proto.SimpleGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "grpc.server.port=0"
)
class GrpcServerServiceIntegrationTests {

    @Autowired
    private GrpcServerLifecycle grpcServerLifecycle;

    private static ManagedChannel newPlaintextChannel(int port) {
        return ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
    }

    @Test
    void sayHelloReturnsExpectedPayload() throws InterruptedException {
        ManagedChannel channel = newPlaintextChannel(grpcServerLifecycle.getBoundPort());
        try {
            SimpleGrpc.SimpleBlockingStub stub = SimpleGrpc.newBlockingStub(channel);
            HelloReply reply = stub.sayHello(HelloRequest.newBuilder().setName("POC").build());
            assertThat(reply.getMessage()).isEqualTo("Hello ==> POC");
        } finally {
            channel.shutdownNow();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void chatHelloExchangesMultipleMessagesOnOneConnection() throws InterruptedException {
        ManagedChannel channel = newPlaintextChannel(grpcServerLifecycle.getBoundPort());
        CountDownLatch done = new CountDownLatch(1);
        List<String> responses = new ArrayList<>();

        try {
            SimpleGrpc.SimpleStub stub = SimpleGrpc.newStub(channel);
            StreamObserver<HelloReply> responseObserver = new StreamObserver<>() {
                @Override
                public void onNext(HelloReply value) {
                    responses.add(value.getMessage());
                }

                @Override
                public void onError(Throwable throwable) {
                    done.countDown();
                }

                @Override
                public void onCompleted() {
                    done.countDown();
                }
            };

            StreamObserver<HelloRequest> requestObserver = stub.chatHello(responseObserver);
            requestObserver.onNext(HelloRequest.newBuilder().setName("A").build());
            requestObserver.onNext(HelloRequest.newBuilder().setName("B").build());
            requestObserver.onNext(HelloRequest.newBuilder().setName("C").build());
            requestObserver.onCompleted();

            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(responses).containsExactly(
                    "Hello(stream) ==> A",
                    "Hello(stream) ==> B",
                    "Hello(stream) ==> C"
            );
        } finally {
            channel.shutdownNow();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}

