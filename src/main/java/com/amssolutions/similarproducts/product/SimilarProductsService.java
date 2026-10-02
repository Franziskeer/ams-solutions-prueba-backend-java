package com.amssolutions.similarproducts.product;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Service;

@Service
class SimilarProductsService {

    private final ProductClient productClient;
    private final ExecutorService executor;

    SimilarProductsService(ProductClient productClient, ExecutorService executor) {
        this.productClient = productClient;
        this.executor = executor;
    }

    List<ProductDetail> getSimilarProducts(String productId) {
        List<String> similarIds = productClient.getSimilarIds(productId);

        List<CompletableFuture<ProductDetail>> futures = similarIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> productClient.getProduct(id), executor))
                .toList();

        return futures.stream()
                .map(future -> future.join())
                .toList();
    }
}