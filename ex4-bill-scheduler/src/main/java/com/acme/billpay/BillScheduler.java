package com.acme.billpay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Holds bills waiting to be paid. */
public class BillScheduler {

    private final List<Bill> bills = new ArrayList<>();

    public void schedule(Bill bill) {
        for (Bill b : bills) {
            if (b.id().equals(bill.id())) {
                throw new IllegalArgumentException("Bill already scheduled: " + bill.id());
            }
        }
        bills.add(bill);
    }

    /** Bills due on exactly this date, largest first. */
    public List<Bill> billsDueOn(java.time.LocalDate date) {
        List<Bill> result = new ArrayList<>();
        for (Bill b : bills) {
            if (b.dueDate().equals(date)) {
                result.add(b);
            }
        }
        result.sort(Comparator.comparingLong(Bill::amountCents).reversed());
        return result;
    }

    public void markPaid(String billId) {
        bills.removeIf(b -> b.id().equals(billId));
    }

    public List<Bill> unpaidBills() {
        return List.copyOf(bills);
    }
}
