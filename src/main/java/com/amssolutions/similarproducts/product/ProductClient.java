package com.amssolutions.similarproducts.product;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
class ProductClient {

    private final RestClient restClient;

    ProductClient(RestClient restClient) {
        this.restClient = restClient;
    }

    List<String> getSimilarIds(String productId) {
        try {
            return restClient.get()
                    .uri("/product/{productId}/similarids", productId)
                    .retrieve()
                    .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND), (request, response) -> {
                        throw new ProductNotFoundException(productId);
                    })
                    .onStatus(status -> status.isError(), (request, response) -> {
                        throw new ProductUpstreamException(productId);
                    })
                    .body(new ParameterizedTypeReference<List<String>>() {
                    });
        } catch (ResourceAccessException e) {
            throw new ProductUpstreamTimeoutException(productId, e);
        }
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
