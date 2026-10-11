package com.ecommerce.order.common;

/** Pesanan valid secara format, tetapi tidak dapat diproses (422). */
public class OrderRejectedException extends RuntimeException {

    public OrderRejectedException(String message) {
        super(message);
    }
}
