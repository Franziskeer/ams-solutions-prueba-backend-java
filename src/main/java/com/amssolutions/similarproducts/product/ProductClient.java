package com.amssolutions.similarproducts.product;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
class ProductClient {

    private final RestClient restClient;
    private final Duration waitTimeout;
    private final AsyncLoadingCache<String, List<String>> similarIdsCache;
    private final AsyncLoadingCache<String, ProductDetail> productCache;

    ProductClient(RestClient restClient, MocksProperties properties, ExecutorService executor) {
        this.restClient = restClient;
        this.waitTimeout = properties.readTimeout();
        this.similarIdsCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.cacheTtl())
                .maximumSize(properties.cacheMaxSize())
                .buildAsync((id, exec) -> CompletableFuture.supplyAsync(() -> fetchSimilarIds(id), executor));
        this.productCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.cacheTtl())
                .maximumSize(properties.cacheMaxSize())
                .buildAsync((id, exec) -> CompletableFuture.supplyAsync(() -> fetchProduct(id), executor));
    }

    List<String> getSimilarIds(String productId) {
        return await(similarIdsCache.get(productId), productId);
    }

    ProductDetail getProduct(String productId) {
        return await(productCache.get(productId), productId);
    }

    private <T> T await(CompletableFuture<T> future, String productId) {
        try {
            return future.get(waitTimeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            throw new ProductUpstreamTimeoutException(productId, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new CompletionException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProductUpstreamTimeoutException(productId, e);
        }
    }

    private List<String> fetchSimilarIds(String productId) {
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

    private ProductDetail fetchProduct(String productId) {
        return restClient.get()
                .uri("/product/{productId}", productId)
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND), (request, response) -> {
                    throw new ProductNotFoundException(productId);
                })
                .body(ProductDetail.class);
    }
}
