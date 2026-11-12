# ecommerce-events

Shared event contracts for every microservice in the platform. This is **not**
a service — it has no `main()`, no Spring context, nothing that runs. It's a
library of plain Java records that get published as JSON and define what
those JSON payloads look like.

## Why this exists

If Order-service and Payment-service each write their own `OrderPlacedEvent`
class, the two will eventually disagree — a renamed field, a `String` vs
`UUID` mismatch, a forgotten field. Nothing catches this at compile time
because the compiler doesn't know the two classes are "the same thing." It
breaks at runtime, in production, as a deserialization error.

The fix: one module, one set of classes, every service imports it. There's
only one `OrderPlacedEvent` in existence — it's not possible for it to drift
from itself.

## What's in here

```
src/main/java/com/yourapp/events/
├── common/
│   ├── DomainEvent.java        interface: eventId() + occurredAt(), shared by every event
│   └── OrderItem.java          shared line-item shape (used inside OrderPlacedEvent)
├── order/
│   └── OrderPlacedEvent.java   published by Order-service
└── payment/
    └── PaymentProcessedEvent.java   published by Payment-service
```

Rules this module follows on purpose:
- **Java records only** — immutable, no setters, no way to mutate an event
  after it's built.
- **`@JsonProperty` on every field** — locks the wire format explicitly,
  instead of relying on field names matching by accident.
- **No Spring, no business logic** — just shapes. Any JVM service can depend
  on this, Spring or not.
- **`DomainEvent` interface** — every event carries an `eventId` (dedupe key)
  and `occurredAt` (when it happened in the domain, not when it was published).

## Using it from another service

1. Build and install this module locally so other projects can resolve it:

   ```bash
   cd ecommerce-events
   mvn clean install
   ```

   (This puts it in your local `~/.m2` repository. In a real team setup
   you'd publish it to a shared artifact repository — Nexus, Artifactory,
   GitHub Packages — instead of relying on everyone's local `.m2`.)

2. Add it as a dependency in Order-service's / Payment-service's `pom.xml`:

   ```xml
   <dependency>
       <groupId>com.yourapp</groupId>
       <artifactId>ecommerce-events</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

3. Use the exact same class to publish and to consume:

   ```java
   // Order-service: publishing
   var event = new OrderPlacedEvent(
       UUID.randomUUID(),
       Instant.now(),
       orderId,
       customerId,
       items,
       totalAmount,
       "USD"
   );
   kafkaTemplate.send("order-placed", objectMapper.writeValueAsString(event));
   ```

   ```java
   // Payment-service: consuming the exact same class
   OrderPlacedEvent event = objectMapper.readValue(json, OrderPlacedEvent.class);
   ```

   Both sides are reading from one definition. If a field changes, it
   changes for everyone at once, and the compiler will tell every dependent
   service the moment they rebuild.

## Changing an event later (without breaking consumers)

- **Adding a field** is safe — old consumers ignore fields they don't know
  about (Jackson does this by default).
- **Removing or renaming a field** is a breaking change — bump the major
  version and coordinate the rollout, or add the new field alongside the old
  one and deprecate the old one first.
- **Changing a field's type** is always breaking. Don't.

## Next steps from here

- Add the rest of the saga's events (`InventoryReservedEvent`,
  `OrderShippedEvent`, etc.) following the same pattern.
- Wire up a real publish/consume path in Order-service and Payment-service
  using this jar.
- Consider Avro + a schema registry once the number of services/events grows
  — it enforces this same "one contract" idea but checks compatibility
  automatically on every publish, instead of relying on people remembering
  the rules above.
