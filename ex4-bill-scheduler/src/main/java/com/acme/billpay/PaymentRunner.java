package com.acme.billpay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The daily payment job. Ops runs it once per bank business day; it pays everything due that day.
 */
public class PaymentRunner {

    private final BillScheduler scheduler;
    private final PaymentGateway gateway;
    private final BusinessCalendar calendar;

    public PaymentRunner(BillScheduler scheduler, PaymentGateway gateway, BusinessCalendar calendar) {
        this.scheduler = scheduler;
        this.gateway = gateway;
        this.calendar = calendar;
    }

    public List<Payment> run(LocalDate today) {
        List<Payment> payments = new ArrayList<>();
        for (Bill bill : scheduler.billsDueOn(today)) {
            String confirmation = gateway.pay(bill.vendor(), bill.amountCents());
            scheduler.markPaid(bill.id());
            payments.add(new Payment(bill.id(), bill.vendor(), bill.amountCents(), confirmation, today));
        }
        return payments;
    }
}
