package com.amssolutions.similarproducts.product;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@WireMockTest
class ProductClientTest {

    private ExecutorService executor;

    private ProductClient client;

    private ProductClient newClient(String baseUrl, Duration waitTimeout) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        MocksProperties properties = new MocksProperties(
                URI.create(baseUrl),
                Duration.ofMillis(500),
                waitTimeout,
                Duration.ofSeconds(60),
                Duration.ofMinutes(1),
                1000);
        return new ProductClient(restClient, properties, executor);
    }

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        client = newClient(wireMock.getHttpBaseUrl(), Duration.ofSeconds(2));
    }

    @AfterEach
    void tearDown() {
        executor.close();
    }

    @Test
    void returnsSimilarIdsInOrderAsStrings() {
        stubFor(get("/product/1/similarids").willReturn(okJson("[2,3,4]")));
        assertThat(client.getSimilarIds("1")).containsExactly("2", "3", "4");
    }

    @Test
    void throwsProductNotFoundWhenSimilarIdsReturns404() {
        stubFor(get("/product/7/similarids").willReturn(notFound()));
        assertThatThrownBy(() -> client.getSimilarIds("7"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void returnsProductDetail() {
        stubFor(get("/product/1").willReturn(okJson("""
                {"id":"1","name":"Shirt","price":9.99,"availability":true}
                """)));
        assertThat(client.getProduct("1"))
                .isEqualTo(new ProductDetail("1", "Shirt", new BigDecimal("9.99"), true));
    }

    @Test
    void throwsProductNotFoundWhenProductReturns404() {
        stubFor(get("/product/5").willReturn(notFound()));
        assertThatThrownBy(() -> client.getProduct("5"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void throwsWhenProductTakesLongerThanReadTimeout(WireMockRuntimeInfo wireMock) {
        stubFor(get("/product/1000").willReturn(okJson("""
                {"id":"1000","name":"Coat","price":89.99,"availability":true}
                """).withFixedDelay(1000)));

        ProductClient timedOutClient = newClient(wireMock.getHttpBaseUrl(), Duration.ofMillis(200));

        assertThatThrownBy(() -> timedOutClient.getProduct("1000"))
                .isInstanceOf(ProductUpstreamTimeoutException.class);
    }

    @Test
    void throwsUpstreamErrorWhenSimilarIdsReturns500() {
        stubFor(get("/product/8/similarids").willReturn(serverError()));

        assertThatThrownBy(() -> client.getSimilarIds("8"))
                .isInstanceOf(ProductUpstreamException.class);
    }

    @Test
    void throwsUpstreamTimeoutWhenSimilarIdsTakesTooLong(WireMockRuntimeInfo wireMock) {
        stubFor(get("/product/9/similarids").willReturn(okJson("[1]").withFixedDelay(1000)));

        ProductClient timedOutClient = newClient(wireMock.getHttpBaseUrl(), Duration.ofMillis(200));

        assertThatThrownBy(() -> timedOutClient.getSimilarIds("9"))
                .isInstanceOf(ProductUpstreamTimeoutException.class);
    }

    @Test
    void reusesInFlightRequestForTheSameProduct(WireMockRuntimeInfo wireMock) {
        stubFor(get("/product/100").willReturn(okJson("""
                {"id":"100","name":"Trousers","price":49.99,"availability":false}
                """).withFixedDelay(300)));

        ProductClient timed = newClient(wireMock.getHttpBaseUrl(), Duration.ofSeconds(2));

        CompletableFuture<ProductDetail> first = CompletableFuture.supplyAsync(() -> timed.getProduct("100"));
        CompletableFuture<ProductDetail> second = CompletableFuture.supplyAsync(() -> timed.getProduct("100"));
        CompletableFuture.allOf(first, second).join();

        assertThat(first.join().name()).isEqualTo("Trousers");
        assertThat(second.join().name()).isEqualTo("Trousers");
        verify(exactly(1), getRequestedFor(urlEqualTo("/product/100")));
    }

    @Test
    void fillsCacheAfterTimeoutSoTheNextCallIsFast(WireMockRuntimeInfo wireMock) throws Exception {
        stubFor(get("/product/1000").willReturn(okJson("""
                {"id":"1000","name":"Coat","price":89.99,"availability":true}
                """).withFixedDelay(400)));

        ProductClient timed = newClient(wireMock.getHttpBaseUrl(), Duration.ofMillis(100));

        assertThatThrownBy(() -> timed.getProduct("1000"))
                .isInstanceOf(ProductUpstreamTimeoutException.class);

        Thread.sleep(500);

        long start = System.nanoTime();
        assertThat(timed.getProduct("1000").name()).isEqualTo("Coat");
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofMillis(200));

        verify(exactly(1), getRequestedFor(urlEqualTo("/product/1000")));
    }
}
