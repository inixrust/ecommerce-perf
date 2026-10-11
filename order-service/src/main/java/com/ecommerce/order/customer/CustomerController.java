package com.ecommerce.order.customer;

import java.sql.SQLException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    private final CustomerSummaryService summaries;

    public CustomerController(CustomerSummaryService summaries) {
        this.summaries = summaries;
    }

    @GetMapping("/api/v1/customers/{customerId}/summary")
    public CustomerSummary summary(@PathVariable Long customerId)
            throws SQLException {
        return summaries.summary(customerId);
    }
}
