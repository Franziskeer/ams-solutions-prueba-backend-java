package com.amssolutions.similarproducts.product;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mocks")
public record MocksProperties(URI baseUrl) {
}
