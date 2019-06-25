package micronaut.grpc.graal.endpoint;

import io.grpc.stub.StreamObserver;
import javax.inject.Singleton;
import micronaut.grpc.graal.grpc.ExampleReply;
import micronaut.grpc.graal.grpc.ExampleRequest;
import micronaut.grpc.graal.grpc.ExampleServiceGrpc;

@Singleton
public class ExampleEndpoint extends ExampleServiceGrpc.ExampleServiceImplBase {

  @Override
  public void send(ExampleRequest request,
      StreamObserver<ExampleReply> responseObserver) {
    ExampleReply reply = ExampleReply.newBuilder()
        .setMessage("Hi " + request.getName())
        .build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }

}
