package com.smarthome.edge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class EdgeGatewayConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "edgeSyncExecutor")
    public Executor edgeSyncExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Bean(name = "cloudSyncExecutor")
    public Executor cloudSyncExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}