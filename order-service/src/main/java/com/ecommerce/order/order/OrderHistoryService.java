package com.ecommerce.order.order;

import com.ecommerce.order.common.NotFoundException;
import com.ecommerce.order.common.PageResponse;
import com.ecommerce.order.customer.CustomerRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class OrderHistoryService {

    private final OrderRepository orders;
    private final CustomerRepository customers;

    public OrderHistoryService(OrderRepository orders,
            CustomerRepository customers) {
        this.orders = orders;
        this.customers = customers;
    }

    public PageResponse<OrderSummary> history(Long customerId, int page,
            int size) {
        if (!customers.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        var result = orders.findByCustomerIdOrderByCreatedAtDesc(
                customerId, PageRequest.of(page, size));
        return PageResponse.of(result.map(OrderHistoryService::toSummary));
    }

    private static OrderSummary toSummary(CustomerOrder order) {
        List<OrderSummary.Line> lines = order.getItems().stream()
                .map(i -> new OrderSummary.Line(i.getProductId(),
                        i.getQuantity(), i.getUnitPrice()))
                .toList();
        return new OrderSummary(order.getId(), order.getStatus(),
                order.getTotal(), order.getCreatedAt(), lines);
    }
}
