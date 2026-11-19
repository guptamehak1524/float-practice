package com.acme.ledger;

import java.time.Instant;

public record Transfer(String id, String fromAccountId, String toAccountId, long amountCents, String memo, Instant at) {
}
