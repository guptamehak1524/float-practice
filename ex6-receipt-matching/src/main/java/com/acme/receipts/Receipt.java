package com.acme.receipts;

import java.time.LocalDate;

/** A receipt an employee photographed in the app. Fields come from OCR, so {@code merchant} is often short, e.g. "Starbucks". */
public record Receipt(String id, String employeeId, String merchant, long amountCents, LocalDate date) {
}
