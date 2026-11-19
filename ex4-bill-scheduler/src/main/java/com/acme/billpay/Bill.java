package com.acme.billpay;

import java.time.LocalDate;
import java.util.Objects;

/** A vendor bill a customer has scheduled for payment. */
public record Bill(String id, String vendor, long amountCents, LocalDate dueDate) {
    public Bill {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(dueDate, "dueDate");
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be positive");
        }
    }
}
