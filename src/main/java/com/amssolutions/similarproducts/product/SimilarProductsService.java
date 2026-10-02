package com.amssolutions.similarproducts.product;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class SimilarProductsService {

    private static final Logger log = LoggerFactory.getLogger(SimilarProductsService.class);

    private final ProductClient productClient;
    private final ExecutorService executor;

    SimilarProductsService(ProductClient productClient, ExecutorService executor) {
        this.productClient = productClient;
        this.executor = executor;
    }

    List<ProductDetail> getSimilarProducts(String productId) {
        List<String> similarIds = productClient.getSimilarIds(productId);

        List<CompletableFuture<Optional<ProductDetail>>> futures = similarIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> fetchDetail(id), executor))
                .toList();

        return futures.stream()
                .map(future -> future.join())
                .flatMap(detail -> detail.stream())
                .toList();
    }

    private Optional<ProductDetail> fetchDetail(String productId) {
        try {
            return Optional.of(productClient.getProduct(productId));
        } catch (RuntimeException e) {
            log.warn("Omitting similar product {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }
}