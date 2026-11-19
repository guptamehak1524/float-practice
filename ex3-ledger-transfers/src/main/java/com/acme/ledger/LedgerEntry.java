package com.acme.ledger;

import java.time.Instant;

/**
 * One immutable line in the ledger. Positive amounts are credits (money in),
 * negative amounts are debits (money out). Entries are never edited or deleted.
 */
public record LedgerEntry(String id, String accountId, long amountCents, String description, Instant at) {
}
