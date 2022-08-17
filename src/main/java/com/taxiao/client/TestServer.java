package com.taxiao.client;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @Author: hanqq
 * @Date: 2022/8/9 23:40
 */
public class TestServer {
    private static Logger logger = Logger.getLogger(TestServer.class.getName());
    private Server server;

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final TestServer server = new TestServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new HelloImpl())
                // 开启反射
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    TestServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });

    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }


    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class HelloImpl extends HelloGrpc.HelloImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            logger.info("hello server: " + request.getName() + " Platform: " + request.getPlatform());
            HelloReply helloReply = HelloReply.newBuilder().setMessage("hello " + request.getName()).setPlatform(Platform.AA).build();
            responseObserver.onNext(helloReply);
            responseObserver.onCompleted();
        }
    }
}
