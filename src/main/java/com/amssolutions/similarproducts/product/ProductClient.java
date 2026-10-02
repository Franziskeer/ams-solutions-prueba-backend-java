package com.amssolutions.similarproducts.product;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class ProductClient {

    private final RestClient restClient;

    ProductClient(RestClient restClient) {
        this.restClient = restClient;
    }

    List<String> getSimilarIds(String productId) {
        return restClient.get()
                .uri("/product/{productId}/similarids", productId)
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND), (request, response) -> {
                    throw new ProductNotFoundException(productId);
                })
                .body(new ParameterizedTypeReference<List<String>>() {
                });
    }

    ProductDetail getProduct(String productId) {
        return restClient.get()
                .uri("/product/{productId}", productId)
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND), (request, response) -> {
                    throw new ProductNotFoundException(productId);
                })
                .body(ProductDetail.class);
    }
}
