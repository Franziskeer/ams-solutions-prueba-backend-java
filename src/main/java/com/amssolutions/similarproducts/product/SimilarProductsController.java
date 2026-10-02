package com.amssolutions.similarproducts.product;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SimilarProductsController {

    private final SimilarProductsService similarProductsService;

    SimilarProductsController(SimilarProductsService similarProductsService) {
        this.similarProductsService = similarProductsService;
    }

    @GetMapping("/product/{productId}/similar")
    List<ProductDetail> getSimilarProducts(@PathVariable String productId) {
        return similarProductsService.getSimilarProducts(productId);
    }
}
