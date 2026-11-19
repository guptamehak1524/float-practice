package com.acme.cards;

import java.util.Objects;

/** A corporate card issued to an employee of a company. */
public record Card(String id, String companyId, long monthlyLimitCents, CardStatus status) {
    public Card {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(status, "status");
        if (monthlyLimitCents < 0) {
            throw new IllegalArgumentException("monthlyLimitCents must be >= 0");
        }
    }
}
