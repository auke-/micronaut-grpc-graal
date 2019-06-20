# micronaut-grpc-graal
Repository to investigate Micronaut GRPC on Graal

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
