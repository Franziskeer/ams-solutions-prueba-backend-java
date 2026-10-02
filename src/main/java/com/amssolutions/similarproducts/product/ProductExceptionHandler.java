package com.amssolutions.similarproducts.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ProductExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<Void> handleProductNotFound() {
        return ResponseEntity.notFound().build();
    }
}
