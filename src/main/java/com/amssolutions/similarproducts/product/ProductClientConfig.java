package com.amssolutions.similarproducts.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class ProductClientConfig {

    @Bean
    RestClient mocksRestClient(RestClient.Builder builder, MocksProperties properties) {
        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }
}
