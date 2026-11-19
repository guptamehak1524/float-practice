package com.acme.billpay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BudgetTest {

    private static final LocalDate WED = LocalDate.of(2026, 11, 18);
    private static final LocalDate THU = LocalDate.of(2026, 11, 19);

    private BillScheduler scheduler;
    private FakeGateway gateway;
    private PaymentRunner runner;

    @BeforeEach
    void setUp() {
        scheduler = new BillScheduler();
        gateway = new FakeGateway();
        runner = new PaymentRunner(scheduler, gateway, new BusinessCalendar(Set.of()));
        scheduler.schedule(new Bill("rent", "Landlord", 3_000_00, WED));
        scheduler.schedule(new Bill("hydro", "Hydro", 200_00, WED));
        scheduler.schedule(new Bill("net", "Internet", 100_00, WED));
    }

    private List<String> ids(List<Payment> payments) {
        return payments.stream().map(Payment::billId).toList();
    }

    @Test
    void paysWhatFitsAndSkipsWhatDoesNot() {
        assertEquals(List.of("hydro", "net"), ids(runner.run(WED, 1_000_00)));
        assertEquals(300_00, gateway.totalPaid);
        assertEquals(List.of("rent"), scheduler.unpaidBills().stream().map(Bill::id).toList());
    }

    @Test
    void skippedBillsAreRetriedNextRun() {
        runner.run(WED, 1_000_00);
        assertEquals(List.of("rent"), ids(runner.run(THU, 5_000_00)));
    }

    @Test
    void exactBudgetIsEnough() {
        assertEquals(List.of("rent", "hydro", "net"), ids(runner.run(WED, 3_300_00)));
    }

    @Test
    void zeroBudgetPaysNothing() {
        assertTrue(runner.run(WED, 0).isEmpty());
        assertEquals(3, scheduler.unpaidBills().size());
    }

    @Test
    void negativeBudgetIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> runner.run(WED, -1));
    }

    @Test
    void noBudgetMeansPayEverything() {
        assertEquals(3, runner.run(WED).size());
    }
}
