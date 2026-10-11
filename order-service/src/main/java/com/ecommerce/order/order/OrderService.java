package com.ecommerce.order.order;

import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PricingClient;
import com.ecommerce.order.client.ProductInfo;
import com.ecommerce.order.common.NotFoundException;
import com.ecommerce.order.common.OrderRejectedException;
import com.ecommerce.order.customer.CustomerRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final CatalogClient catalog;
    private final PricingClient pricing;
    private final TransactionTemplate transaction;

    public OrderService(OrderRepository orders,
            CustomerRepository customers, CatalogClient catalog,
            PricingClient pricing, TransactionTemplate transaction) {
        this.orders = orders;
        this.customers = customers;
        this.catalog = catalog;
        this.pricing = pricing;
        this.transaction = transaction;
    }

    /**
     * Panggilan HTTP dilakukan di luar transaksi: selama menunggu
     * catalog-service dan pricing, tidak ada koneksi pool yang dipegang.
     * Transaksi hanya membungkus penulisan order.
     */
    public OrderView create(CreateOrderRequest request) {
        Long customerId = request.customerId();
        if (!customers.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        // TODO (Latihan 3.3)
        List<PricedLine> lines = new ArrayList<>();
        for (CreateOrderRequest.Line line : request.items()) {
            ProductInfo product = catalog.product(line.productId());
            if (!product.isActive()) {
                throw new OrderRejectedException(
                        "Product " + line.productId()
                                + " is not available");
            }
            BigDecimal price = pricing.price(line.productId());
            lines.add(new PricedLine(line.productId(), line.quantity(),
                    price));
        }
        // TransactionTemplate, bukan @Transactional pada method lain di
        // kelas ini: panggilan this.method() tidak melewati proxy Spring.
        return transaction.execute(status -> save(customerId, lines));
    }

    private OrderView save(Long customerId, List<PricedLine> lines) {
        // getReferenceById: proxy tanpa SELECT, cukup untuk foreign key
        var customer = customers.getReferenceById(customerId);
        var order = new CustomerOrder(customer);
        for (PricedLine line : lines) {
            order.addItem(line.productId(), line.quantity(),
                    line.unitPrice());
        }
        return OrderView.of(orders.save(order));
    }

    private record PricedLine(
            Long productId, int quantity, BigDecimal unitPrice) {
    }
}
