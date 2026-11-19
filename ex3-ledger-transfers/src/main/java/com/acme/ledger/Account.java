package com.acme.ledger;

import java.util.Objects;

/** @param currency ISO code such as "CAD" or "USD" */
public record Account(String id, String ownerName, String currency) {
    public Account {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(currency, "currency");
    }
}
