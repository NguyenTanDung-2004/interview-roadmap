package poc.tech.grpc;

import io.grpc.stub.StreamObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import poc.tech.grpc.proto.HelloReply;
import poc.tech.grpc.proto.HelloRequest;
import poc.tech.grpc.proto.SimpleGrpc;

@Service
class GrpcServerService extends SimpleGrpc.SimpleImplBase {

    private static final Log LOG = LogFactory.getLog(GrpcServerService.class);

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        LOG.info("SayHello for " + request.getName());
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void streamHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        LOG.info("StreamHello for " + request.getName());
        for (int i = 0; i < 3; i++) {
            HelloReply reply = HelloReply.newBuilder()
                    .setMessage("Hello(" + i + ") ==> " + request.getName())
                    .build();
            responseObserver.onNext(reply);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> chatHello(StreamObserver<HelloReply> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(HelloRequest request) {
                LOG.info("ChatHello for " + request.getName());
                HelloReply reply = HelloReply.newBuilder()
                        .setMessage("Hello(stream) ==> " + request.getName())
                        .build();
                responseObserver.onNext(reply);
            }

            @Override
            public void onError(Throwable throwable) {
                LOG.warn("ChatHello stream closed with error", throwable);
            }

            @Override
            public void onCompleted() {
                LOG.info("ChatHello stream completed");
                responseObserver.onCompleted();
            }
        };
    }
}

