package com.amssolutions.similarproducts.product;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SimilarProductsController.class)
class SimilarProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimilarProductsService similarProductsService;

    @Test
    void returnsSimilarProductsAsJson() throws Exception {
        when(similarProductsService.getSimilarProducts("1")).thenReturn(List.of(
                new ProductDetail("2", "Dress", new BigDecimal("19.99"), true),
                new ProductDetail("3", "Blazer", new BigDecimal("29.99"), false)));

        mockMvc.perform(get("/product/1/similar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].name").value("Dress"))
                .andExpect(jsonPath("$[0].price").value(19.99))
                .andExpect(jsonPath("$[0].availability").value(true))
                .andExpect(jsonPath("$[1].id").value("3"))
                .andExpect(jsonPath("$[1].name").value("Blazer"))
                .andExpect(jsonPath("$[1].price").value(29.99))
                .andExpect(jsonPath("$[1].availability").value(false));
    }

    @Test
    void returns404WhenProductDoesNotExist() throws Exception {
        when(similarProductsService.getSimilarProducts("99")).thenThrow(new ProductNotFoundException("99"));

        mockMvc.perform(get("/product/99/similar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Product not found: 99"));
    }

    @Test
    void returns502WhenSimilarIdsFails() throws Exception {
        when(similarProductsService.getSimilarProducts("1")).thenThrow(new ProductUpstreamException("1"));

        mockMvc.perform(get("/product/1/similar"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    void returns504WhenSimilarIdsTimesOut() throws Exception {
        when(similarProductsService.getSimilarProducts("1"))
                .thenThrow(new ProductUpstreamTimeoutException("1", new RuntimeException("timeout")));

        mockMvc.perform(get("/product/1/similar"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.status").value(504));
    }
}
