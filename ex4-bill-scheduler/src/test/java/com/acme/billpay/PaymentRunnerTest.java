package com.acme.billpay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Existing behaviour. These all pass today and must keep passing. */
class PaymentRunnerTest {

    private static final LocalDate WED = LocalDate.of(2026, 11, 18);

    private BillScheduler scheduler;
    private FakeGateway gateway;
    private PaymentRunner runner;

    @BeforeEach
    void setUp() {
        scheduler = new BillScheduler();
        gateway = new FakeGateway();
        runner = new PaymentRunner(scheduler, gateway, new BusinessCalendar(Set.of()));
    }

    @Test
    void paysBillsDueTodayLargestFirst() {
        scheduler.schedule(new Bill("b1", "Hydro", 120_00, WED));
        scheduler.schedule(new Bill("b2", "Landlord", 4_000_00, WED));
        scheduler.schedule(new Bill("b3", "Internet", 90_00, WED.plusDays(1)));

        List<Payment> paid = runner.run(WED);

        assertEquals(List.of("Landlord", "Hydro"), gateway.paidVendors);
        assertEquals(2, paid.size());
        assertEquals("CONF-1", paid.get(0).confirmation());
        assertEquals(WED, paid.get(0).paidOn());
        assertEquals(List.of("b3"), scheduler.unpaidBills().stream().map(Bill::id).toList());
    }

    @Test
    void runningTwiceDoesNotPayTwice() {
        scheduler.schedule(new Bill("b1", "Hydro", 120_00, WED));
        runner.run(WED);
        runner.run(WED);
        assertEquals(120_00, gateway.totalPaid);
    }

    @Test
    void duplicateBillIdIsRejected() {
        scheduler.schedule(new Bill("b1", "Hydro", 120_00, WED));
        assertThrows(IllegalArgumentException.class, () -> scheduler.schedule(new Bill("b1", "Hydro", 120_00, WED)));
    }

    @Test
    void billsMustHavePositiveAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new Bill("b9", "X", 0, WED));
    }

    @Test
    void calendarKnowsWeekendsAndHolidays() {
        BusinessCalendar cal = new BusinessCalendar(Set.of(LocalDate.of(2026, 12, 25)));
        assertTrue(cal.isBusinessDay(WED));
        assertFalse(cal.isBusinessDay(LocalDate.of(2026, 11, 21)));   // Saturday
        assertFalse(cal.isBusinessDay(LocalDate.of(2026, 12, 25)));   // Christmas, a Friday
    }
}
