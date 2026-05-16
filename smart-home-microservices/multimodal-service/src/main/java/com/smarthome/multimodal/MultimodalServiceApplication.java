package com.smarthome.multimodal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MultimodalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultimodalServiceApplication.class, args);
    }
}