package com.yourapp.events.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourapp.events.common.DomainEvent;
import com.yourapp.events.common.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published by Order-service the moment a customer's order is accepted,
 * before payment has been attempted. Payment-service and Inventory-service
 * both consume this to start their side of the saga.
 */
public record OrderPlacedEvent(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("occurredAt") Instant occurredAt,
        @JsonProperty("orderId") String orderId,
        @JsonProperty("customerId") String customerId,
        @JsonProperty("items") List<OrderItem> items,
        @JsonProperty("totalAmount") BigDecimal totalAmount,
        @JsonProperty("currency") String currency
) implements DomainEvent {

    @JsonCreator
    public OrderPlacedEvent {
        // Explicit canonical constructor keeps Jackson deserialization
        // deterministic even if compiled without -parameters.
    }
}
