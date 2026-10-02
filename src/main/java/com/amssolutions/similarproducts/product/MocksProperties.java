package com.amssolutions.similarproducts.product;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mocks")
public record MocksProperties(URI baseUrl, Duration connectTimeout,
        Duration readTimeout,
        Duration httpReadTimeout,
        Duration cacheTtl,
        long cacheMaxSize) {
}
