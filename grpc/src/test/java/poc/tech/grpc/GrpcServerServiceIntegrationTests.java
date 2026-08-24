package poc.tech.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import poc.tech.grpc.proto.HelloReply;
import poc.tech.grpc.proto.HelloRequest;
import poc.tech.grpc.proto.SimpleGrpc;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GrpcServerServiceIntegrationTests {

    @Test
    void sayHelloReturnsExpectedPayload() throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        try {
            SimpleGrpc.SimpleBlockingStub stub = SimpleGrpc.newBlockingStub(channel);
            HelloReply reply = stub.sayHello(HelloRequest.newBuilder().setName("POC").build());
            assertThat(reply.getMessage()).isEqualTo("Hello ==> POC");
        } finally {
            channel.shutdownNow();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}

