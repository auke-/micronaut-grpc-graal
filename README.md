# micronaut-grpc-graal
Repository to investigate Micronaut gRPC on Graal

The project is created with the Micronaut 1.1.3 command line tool:
```bash
$ mn create-app micronaut-grpc-graal --build=maven --profile grpc --features=graal-native-image,java
```

Building that project fails due to the absence of the SubstrateVM substitutes for Netty:
```bash
$ ./mvnw clean package && ./docker-build.sh
...
Warning: RecomputeFieldValue.ArrayIndexScale automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.arrayIndexScale(Class) was detected in the static initializer of io.netty.util.internal.PlatformDependent0. Detailed failure reason(s): The field java.lang.Long.value, where the value produced by the array index scale computation is stored, is not static.
Warning: RecomputeFieldValue.FieldOffset automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.objectFieldOffset(Field) was detected in the static initializer of io.netty.util.internal.PlatformDependent0. Add a RecomputeFieldValue.FieldOffset manual substitution for io.netty.util.internal.PlatformDependent0.ADDRESS_FIELD_OFFSET. Detailed failure reason(s): The argument of Unsafe.objectFieldOffset(Field) is not a constant field.
Warning: RecomputeFieldValue.ArrayIndexScale automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.arrayIndexScale(Class) was detected in the static initializer of io.micronaut.caffeine.cache.UnsafeRefArrayAccess. Detailed failure reason(s): Could not determine the field where the value produced by the call to sun.misc.Unsafe.arrayIndexScale(Class) for the array index scale computation is stored. The call is not directly followed by a field store or by a sign extend node followed directly by a field store.
Warning: RecomputeFieldValue.FieldOffset automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.objectFieldOffset(Field) was detected in the static initializer of io.netty.util.internal.CleanerJava6. Detailed failure reason(s): The argument of Unsafe.objectFieldOffset(Field) is not a constant field., Could not determine the field where the value produced by the call to sun.misc.Unsafe.objectFieldOffset(Field) for the field offset computation is stored. The call is not directly followed by a field store or by a sign extend node followed directly by a field store.
Warning: RecomputeFieldValue.ArrayIndexScale automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.arrayIndexScale(Class) was detected in the static initializer of io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess. Detailed failure reason(s): Could not determine the field where the value produced by the call to sun.misc.Unsafe.arrayIndexScale(Class) for the array index scale computation is stored. The call is not directly followed by a field store or by a sign extend node followed directly by a field store.
Warning: RecomputeFieldValue.FieldOffset automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.objectFieldOffset(Field) was detected in the static initializer of io.netty.buffer.AbstractReferenceCountedByteBuf. Detailed failure reason(s): Could not determine the field where the value produced by the call to sun.misc.Unsafe.objectFieldOffset(Field) for the field offset computation is stored. The call is not directly followed by a field store or by a sign extend node followed directly by a field store.
Warning: class initialization of class io.netty.util.internal.logging.Log4JLogger failed with exception java.lang.NoClassDefFoundError: org/apache/log4j/Priority. This class will be initialized at run time because either option --report-unsupported-elements-at-runtime or option --allow-incomplete-classpath is used for image building. Use the option --initialize-at-run-time=io.netty.util.internal.logging.Log4JLogger to explicitly request delayed initialization of this class.
Warning: class initialization of class io.netty.handler.ssl.ConscryptAlpnSslEngine failed with exception java.lang.NoClassDefFoundError: org/conscrypt/BufferAllocator. This class will be initialized at run time because either option --report-unsupported-elements-at-runtime or option --allow-incomplete-classpath is used for image building. Use the option --initialize-at-run-time=io.netty.handler.ssl.ConscryptAlpnSslEngine to explicitly request delayed initialization of this class.
...
```

Micronaut supplies those substitutes with their Netty components, which are also needed when using the Micronaut rest client.
The micronaut-http-client dependency is added to the pom file:
```xml
<dependency>
  <groupId>io.micronaut</groupId>
  <artifactId>micronaut-http-client</artifactId>
  <scope>compile</scope>
</dependency>
```

This gives a different error when building the native image:
```bash
$ ./mvnw clean package && ./docker-build.sh
...
Warning: Aborting stand-alone image build. Detected a direct/mapped ByteBuffer in the image heap. A direct ByteBuffer has a pointer to unmanaged C memory, and C memory from the image generator is not available at image run time. A mapped ByteBuffer references a file descriptor, which is no longer open and mapped at run time. The object was probably created by a class initializer and is reachable from a static field. By default, all class initialization is done during native image building.You can manually delay class initialization to image run time by using the option -H:ClassInitialization=<class-name>. Or you can write your own initialization methods and call them explicitly from your main entry point.
Detailed message:
Trace: 	object io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf
	object io.netty.buffer.ReadOnlyByteBuf
	object io.netty.buffer.UnreleasableByteBuf
	method io.netty.handler.codec.http2.DefaultHttp2FrameWriter.writeContinuationFrames(ChannelHandlerContext, int, ByteBuf, int, Http2CodecUtil$SimpleChannelPromiseAggregator)
Call path from entry point to io.netty.handler.codec.http2.DefaultHttp2FrameWriter.writeContinuationFrames(ChannelHandlerContext, int, ByteBuf, int, Http2CodecUtil$SimpleChannelPromiseAggregator):
	at io.netty.handler.codec.http2.DefaultHttp2FrameWriter.writeContinuationFrames(DefaultHttp2FrameWriter.java:555)
	at io.netty.handler.codec.http2.DefaultHttp2FrameWriter.writeHeadersInternal(DefaultHttp2FrameWriter.java:534)
	at io.netty.handler.codec.http2.DefaultHttp2FrameWriter.writeHeaders(DefaultHttp2FrameWriter.java:266)
	at io.netty.handler.codec.http2.Http2OutboundFrameLogger.writeHeaders(Http2OutboundFrameLogger.java:60)
	at io.netty.handler.codec.http2.DecoratingHttp2FrameWriter.writeHeaders(DecoratingHttp2FrameWriter.java:53)
	at io.grpc.netty.NettyServerHandler$WriteMonitoringFrameWriter.writeHeaders(NettyServerHandler.java:958)
	at io.netty.handler.codec.http2.DefaultHttp2ConnectionEncoder.writeHeaders(DefaultHttp2ConnectionEncoder.java:205)
	at io.netty.handler.codec.http2.DefaultHttp2ConnectionEncoder.writeHeaders(DefaultHttp2ConnectionEncoder.java:146)
	at io.netty.handler.codec.http2.Http2ConnectionHandler.handleServerHeaderDecodeSizeError(Http2ConnectionHandler.java:720)
	at io.netty.handler.codec.http2.Http2ConnectionHandler.onStreamError(Http2ConnectionHandler.java:696)
	at io.grpc.netty.NettyServerHandler.onStreamError(NettyServerHandler.java:506)
	at io.netty.handler.codec.http2.Http2ConnectionHandler.onError(Http2ConnectionHandler.java:610)
	at io.grpc.netty.NettyServerHandler$GracefulShutdown.secondGoAwayAndClose(NettyServerHandler.java:921)
	at io.grpc.netty.NettyServerHandler$GracefulShutdown$1.run(NettyServerHandler.java:885)
	at com.oracle.svm.core.jdk.RuntimeSupport.executeHooks(RuntimeSupport.java:144)
	at com.oracle.svm.core.jdk.RuntimeSupport.executeStartupHooks(RuntimeSupport.java:89)
	at com.oracle.svm.core.JavaMainWrapper.run(JavaMainWrapper.java:145)
	at com.oracle.svm.core.code.IsolateEnterStub.JavaMainWrapper_run_5087f5482cc9a6abc971913ece43acb471d2631b(generated:0)

Warning: Use -H:+ReportExceptionStackTraces to print stacktrace of underlying exception
```

Currently Micronaut uses Netty version *4.1.30.Final*. The latest version (*4.1.36.Final*) contains
substitutes for GraalVM, which would make the substitutes in Micronaut obsolete. Unfortunately
Micronaut isn't compatible with Netty *4.1.36.Final*. However, the substitutes from this version
can be applied to the current version of Micronaut, especially the two following lines:
```bash
  --rerun-class-initialization-at-runtime=io.netty.handler.codec.http2.Http2CodecUtil
  --initialize-at-run-time=io.netty.handler.codec.http2.DefaultHttp2FrameWriter
```
Those lines are taken from the following Netty change:
https://github.com/netty/netty/commit/f1495e19459eb4e961b1c078e7692680f88a0803#diff-5cbe3026d0fe94bd7b8480f692edfb17R15

When those options are applied to the sample project (by adding them to native-image.properties)
the project builds correctly in both the JVM and as a GraalVM native image. Unfortunately the
application doesn't work because the example project doesn't implement and exposes the example
service. We need to implement the service, e.g. like this:
```java
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
```

With the service enabled the native compilation fails again with the following messages:
```bash
$ ./mvnw clean package && ./docker-build.sh
...
Warning: RecomputeFieldValue.ArrayBaseOffset automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.arrayBaseOffset(Class) was detected in the static initializer of com.google.protobuf.UnsafeUtil. Detailed failure reason(s): Could not determine the field where the value produced by the call to sun.misc.Unsafe.arrayBaseOffset(Class) for the array base offset computation is stored. The call is not directly followed by a field store or by a sign extend node followed directly by a field store.
...
Error: Class that is marked for delaying initialization to run time got initialized during image building: com.google.protobuf.ExtensionRegistry. Try marking this class for build-time initialization with --initialize-at-build-time=com.google.protobuf.ExtensionRegistry
Error: Use -H:+ReportExceptionStackTraces to print stacktrace of underlying exception
Error: Image build request failed with exit status 1
```

It seems like com.google.protobuf.UnsafeUtil and com.google.protobuf.ExtensionRegistry needs some configuration in order to work with GraalVM.

When we mark the whole protobuf package to initialize at build time the image can be build:
```bash
  --rerun-class-initialization-at-runtime=io.netty.handler.codec.http2.Http2CodecUtil \
  --initialize-at-run-time=io.netty.handler.codec.http2.DefaultHttp2FrameWriter \
  --initialize-at-build-time=com.google.protobuf
```

```bash
$ ./mvnw clean package && ./docker-build.sh
...
Warning: Using a deprecated option --rerun-class-initialization-at-runtime. Currently there is no replacement for this option. Try using --initialize-at-run-time or use the non-API option -H:ClassInitialization directly.
...
Warning: RecomputeFieldValue.FieldOffset automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.objectFieldOffset(Field) was detected in the static initializer of com.google.protobuf.UnsafeUtil. Detailed failure reason(s): The argument of Unsafe.objectFieldOffset(Field) is not a constant field., Could not determine the field where the value produced by the call to sun.misc.Unsafe.objectFieldOffset(Field) for the field offset computation is stored. The call is not directly followed by a field store or by a sign extend node followed directly by a field store.
Warning: RecomputeFieldValue.FieldOffset automatic substitution failed. The automatic substitution registration was attempted because a call to sun.misc.Unsafe.objectFieldOffset(Field) was detected in the static initializer of com.google.protobuf.UnsafeUtil. Detailed failure reason(s): The argument of Unsafe.objectFieldOffset(Field) is not a constant field., Could not determine the field where the value produced by the call to sun.misc.Unsafe.objectFieldOffset(Field) for the field offset computation is stored. The call is not directly followed by a field store or by a sign extend node followed directly by a field store.
...
Successfully tagged micronaut-grpc-graal:latest


To run the docker container execute:
    $ docker run -p 8080:8080 micronaut-grpc-graal
```

The gRPC server runs on port 50051, so we have to expose this port when starting the container:
```bash
$ docker run --rm --name micronaut-grpc-graal -p 50051:50051 micronaut-grpc-graal
... [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 20ms. Server Running: http://localhost:50051
```

Now we can send a request to our service, for example with the gRPC CLI:
```bash
docker run --network=host -v $(pwd)/src/main/proto:/proto --rm -it namely/grpc-cli \
  call localhost:50051 micronaut.grpc.graal.ExampleService.send \
  "name: 'Me'" --proto_path=/proto/ --protofiles=example.proto --remotedb=false
```
