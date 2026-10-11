package com.ecommerce.order.order;

import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PricingClient;
import com.ecommerce.order.client.ProductInfo;
import com.ecommerce.order.common.NotFoundException;
import com.ecommerce.order.common.OrderRejectedException;
import com.ecommerce.order.customer.Customer;
import com.ecommerce.order.customer.CustomerRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final CatalogClient catalog;
    private final PricingClient pricing;

    public OrderService(OrderRepository orders,
            CustomerRepository customers, CatalogClient catalog,
            PricingClient pricing) {
        this.orders = orders;
        this.customers = customers;
        this.catalog = catalog;
        this.pricing = pricing;
    }

    @Transactional
    public CustomerOrder create(CreateOrderRequest request) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException(
                        "Customer", request.customerId()));
        CustomerOrder order = new CustomerOrder(customer);
        for (CreateOrderRequest.Line line : request.items()) {
            ProductInfo product = catalog.product(line.productId());
            if (!product.isActive()) {
                throw new OrderRejectedException(
                        "Product " + line.productId()
                                + " is not available");
            }
            BigDecimal price = pricing.price(line.productId());
            order.addItem(line.productId(), line.quantity(), price);
        }
        return orders.save(order);
    }
}
