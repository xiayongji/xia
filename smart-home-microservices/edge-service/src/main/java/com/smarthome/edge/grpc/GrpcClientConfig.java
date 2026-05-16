package com.smarthome.edge.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

@Slf4j
@Configuration
public class GrpcClientConfig {

    @Value("${grpc.server.port:9090}")
    private int grpcServerPort;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel managedChannel() {
        channel = ManagedChannelBuilder.forAddress("localhost", grpcServerPort)
                .usePlaintext()
                .build();
        log.info("gRPC 客户端通道已创建: localhost:{}", grpcServerPort);
        return channel;
    }

    @Bean
    public DeviceControlServiceGrpc.DeviceControlServiceBlockingStub deviceControlStub(ManagedChannel channel) {
        return DeviceControlServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public DeviceControlServiceGrpc.DeviceControlServiceStub deviceControlAsyncStub(ManagedChannel channel) {
        return DeviceControlServiceGrpc.newStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("gRPC 客户端通道已关闭");
        }
    }
}