package com.amssolutions.similarproducts.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class SimilarProductsServiceTest {

    private static final ProductDetail DRESS = new ProductDetail("2", "Dress", new BigDecimal("19.99"), true);
    private static final ProductDetail BLAZER = new ProductDetail("3", "Blazer", new BigDecimal("29.99"), false);
    private static final ProductDetail BOOTS = new ProductDetail("4", "Boots", new BigDecimal("39.99"), true);

    @Mock
    private ProductClient productClient;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private SimilarProductsService service;

    @BeforeEach
    void setUp() {
        service = new SimilarProductsService(productClient, executor);
    }

    @AfterEach
    void tearDown() {
        executor.close();
    }

    @Test
    void keepsSimilarityOrderEvenWhenDetailsArriveOutOfOrder() {
        when(productClient.getSimilarIds("1")).thenReturn(List.of("2", "3", "4"));
        when(productClient.getProduct("2")).thenAnswer(after(Duration.ofMillis(300), DRESS));
        when(productClient.getProduct("3")).thenReturn(BLAZER);
        when(productClient.getProduct("4")).thenAnswer(after(Duration.ofMillis(100), BOOTS));

        assertThat(service.getSimilarProducts("1")).containsExactly(DRESS, BLAZER, BOOTS);
    }

    @Test
    void fetchesDetailsInParallel() {
        when(productClient.getSimilarIds("1")).thenReturn(List.of("2", "3", "4"));
        when(productClient.getProduct("2")).thenAnswer(after(Duration.ofMillis(300), DRESS));
        when(productClient.getProduct("3")).thenAnswer(after(Duration.ofMillis(200), BLAZER));
        when(productClient.getProduct("4")).thenAnswer(after(Duration.ofMillis(100), BOOTS));

        long start = System.nanoTime();
        service.getSimilarProducts("1");
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertThat(elapsed).isLessThan(Duration.ofMillis(1000));
    }

    @Test
    void returnsEmptyListWhenThereAreNoSimilarProducts() {
        when(productClient.getSimilarIds("1")).thenReturn(List.of());

        assertThat(service.getSimilarProducts("1")).isEmpty();
        verify(productClient, never()).getProduct(any());
    }

    @Test
    void propagatesProductNotFoundWhenProductDoesNotExist() {
        when(productClient.getSimilarIds("99")).thenThrow(new ProductNotFoundException("99"));

        assertThatThrownBy(() -> service.getSimilarProducts("99"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    private static Answer<ProductDetail> after(Duration delay, ProductDetail product) {
        return invocation -> {
            Thread.sleep(delay);
            return product;
        };
    }
}
