package com.yourapp.events.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * One line item on an order. Shared by OrderPlacedEvent and the inventory
 * events so a reservation always lines up with what was actually ordered.
 */
public record OrderItem(
        @JsonProperty("productId") String productId,
        @JsonProperty("productName") String productName,
        @JsonProperty("quantity") int quantity,
        @JsonProperty("unitPrice") BigDecimal unitPrice
) {
}
