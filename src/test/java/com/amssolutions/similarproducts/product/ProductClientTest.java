package com.amssolutions.similarproducts.product;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@WireMockTest
class ProductClientTest {

    private ProductClient client;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        RestClient restClient = RestClient.builder()
                .baseUrl(wireMock.getHttpBaseUrl())
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
        client = new ProductClient(restClient);
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
}
