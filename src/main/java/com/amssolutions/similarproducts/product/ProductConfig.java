package com.amssolutions.similarproducts.product;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class ProductConfig {

    @Bean
    RestClient mocksRestClient(RestClient.Builder builder, MocksProperties properties) {
        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }

    @Bean
    ExecutorService productDetailsExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
