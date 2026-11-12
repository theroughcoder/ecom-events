package com.yourapp.events.common;

import java.time.Instant;
import java.util.UUID;

/**
 * Every event in this library implements this. Two fields, non-negotiable:
 *
 *   - eventId    -> your idempotency key. Consumers use this to detect and
 *                   drop duplicate deliveries (Kafka gives "at least once",
 *                   not "exactly once" — this is how you fake the latter).
 *   - occurredAt -> when the event actually happened in the domain, not
 *                   when it was published or consumed. Lets you debug
 *                   ordering issues across partitions and services.
 *
 * Records implementing this interface automatically satisfy it just by
 * naming their components eventId() and occurredAt() — no boilerplate.
 */
public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
}
