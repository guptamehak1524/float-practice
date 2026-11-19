package com.acme.receipts;

import java.time.LocalDate;

/** A card transaction as reported by the card network. {@code merchant} is the raw descriptor, e.g. "STARBUCKS #4521 TORONTO ON". */
public record CardTransaction(String id, String employeeId, String merchant, long amountCents, LocalDate date) {
}
