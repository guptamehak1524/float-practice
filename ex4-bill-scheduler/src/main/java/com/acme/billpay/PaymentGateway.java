package com.acme.billpay;

/** Sends money to a vendor through the bank (EFT). Returns the bank's confirmation number. */
public interface PaymentGateway {
    String pay(String vendor, long amountCents);
}
