package com.acme.billpay;

import java.time.LocalDate;

public record Payment(String billId, String vendor, long amountCents, String confirmation, LocalDate paidOn) {
}
