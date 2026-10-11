package com.ecommerce.order.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    Page<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId, Pageable pageable);
}
