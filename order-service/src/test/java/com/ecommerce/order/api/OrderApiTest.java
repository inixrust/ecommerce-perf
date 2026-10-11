package com.ecommerce.order.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.order.support.IntegrationTest;
import com.ecommerce.order.support.Stubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Kontrak fungsional order-service; harus hijau di setiap tag. */
@IntegrationTest
class OrderApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    Stubs stubs;

    @BeforeEach
    void setUp() {
        stubs.reset();
        stubs.token(60, 0);
    }

    @Test
    void createOrderPricesEveryLine() throws Exception {
        stubs.product(101, "ACTIVE");
        stubs.product(102, "ACTIVE");
        stubs.price(101, "10000.00", 0);
        stubs.price(102, "2500.50", 0);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 3, "items": [
                                  {"productId": 101, "quantity": 2},
                                  {"productId": 102, "quantity": 1}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.total").value(22500.50))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void inactiveProductIsRejected() throws Exception {
        stubs.product(103, "INACTIVE");
        stubs.price(103, "1000.00", 0);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 3, "items": [
                                  {"productId": 103, "quantity": 1}]}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void unknownProductIsRejected() throws Exception {
        stubs.unknownProduct(104);

        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 3, "items": [
                                  {"productId": 104, "quantity": 1}]}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void unknownCustomerIsNotFound() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 999, "items": [
                                  {"productId": 101, "quantity": 1}]}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void historyIsNewestFirst() throws Exception {
        mvc.perform(get("/api/v1/customers/1/orders")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.content[0].createdAt")
                        .value("2026-01-02T06:00:00Z"))
                .andExpect(jsonPath("$.content[0].items", hasSize(3)));
    }

    @Test
    void historyOfUnknownCustomerIsNotFound() throws Exception {
        mvc.perform(get("/api/v1/customers/999/orders"))
                .andExpect(status().isNotFound());
    }
}
