package com.amssolutions.similarproducts.product;

public class ProductUpstreamException extends RuntimeException {

    public ProductUpstreamException(String productId) {
        super("Upstream error for product " + productId);
    }
}
