package com.acme.billpay;

import java.util.ArrayList;
import java.util.List;

/** Records every payment instead of calling a bank. */
class FakeGateway implements PaymentGateway {
    final List<String> paidVendors = new ArrayList<>();
    long totalPaid = 0;

    @Override
    public String pay(String vendor, long amountCents) {
        paidVendors.add(vendor);
        totalPaid += amountCents;
        return "CONF-" + paidVendors.size();
    }
}
