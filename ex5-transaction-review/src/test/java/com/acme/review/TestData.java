package com.acme.review;

import java.time.Instant;

final class TestData {
    private TestData() {}

    static final Instant WEDNESDAY_NOON_TORONTO = Instant.parse("2026-11-18T17:00:00Z");
    private static int nextId = 1;

    static Transaction txn(long amountCents, String merchant, String mcc, String country) {
        return txn("card-1", amountCents, merchant, mcc, country, WEDNESDAY_NOON_TORONTO);
    }

    static Transaction txn(String cardId, long amountCents, String merchant, String mcc, String country, Instant at) {
        return new Transaction("t" + nextId++, cardId, merchant, mcc, amountCents, at, country);
    }
}
