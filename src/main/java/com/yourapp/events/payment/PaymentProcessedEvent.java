package com.yourapp.events.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourapp.events.common.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Published by Payment-service once a payment attempt for an order has been
 * settled (successfully or not). Order-service consumes this to move the
 * order out of "awaiting payment" and into "confirmed" or "payment failed".
 *
 * This is the other half of the contract problem described in DomainEvent:
 * Order-service didn't write this class, Payment-service did — but because
 * both depend on this same jar, there's exactly one definition either side
 * can drift from. There's nothing to drift to.
 */
public record PaymentProcessedEvent(
        @JsonProperty("eventId") UUID eventId,
        @JsonProperty("occurredAt") Instant occurredAt,
        @JsonProperty("orderId") String orderId,
        @JsonProperty("paymentId") String paymentId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("status") PaymentStatus status,
        @JsonProperty("failureReason") String failureReason
) implements DomainEvent {

    @JsonCreator
    public PaymentProcessedEvent {
        // Explicit canonical constructor; see OrderPlacedEvent for why.
    }

    /**
     * Plain enum, not a boolean — leaves room for PENDING/REFUNDED later
     * without changing the event's shape (and breaking old consumers).
     */
    public enum PaymentStatus {
        SUCCEEDED,
        FAILED
    }
}
