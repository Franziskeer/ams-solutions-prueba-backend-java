package com.amssolutions.similarproducts.product;

public class ProductUpstreamTimeoutException extends RuntimeException {

    public ProductUpstreamTimeoutException(String productId, Throwable cause) {
        super("Upstream timeout for product " + productId, cause);
    }
}