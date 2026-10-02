package com.amssolutions.similarproducts.product;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
class SimilarProductsService {

    private final ProductClient productClient;

    SimilarProductsService(ProductClient productClient) {
        this.productClient = productClient;
    }

    List<ProductDetail> getSimilarProducts(String productId) {
        List<String> similarIds = productClient.getSimilarIds(productId);
        return similarIds.stream()
                .map(productClient::getProduct)
                .toList();
    }
}