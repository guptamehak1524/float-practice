package com.acme.billpay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecurringBillTest {

    private BillScheduler scheduler;
    private PaymentRunner runner;

    @BeforeEach
    void setUp() {
        scheduler = new BillScheduler();
        runner = new PaymentRunner(scheduler, new FakeGateway(), new BusinessCalendar(Set.of()));
    }

    private Bill only() {
        List<Bill> unpaid = scheduler.unpaidBills();
        assertEquals(1, unpaid.size());
        return unpaid.get(0);
    }

    @Test
    void firstInstanceIsScheduled() {
        scheduler.scheduleMonthly("rent", "Landlord", 3_000_00, 1, YearMonth.of(2026, 12));
        Bill b = only();
        assertEquals("rent-2026-12", b.id());
        assertEquals(LocalDate.of(2026, 12, 1), b.dueDate());
    }

    @Test
    void payingABillSchedulesNextMonth() {
        scheduler.scheduleMonthly("rent", "Landlord", 3_000_00, 1, YearMonth.of(2026, 12));
        runner.run(LocalDate.of(2026, 12, 1));             // Tuesday
        Bill next = only();
        assertEquals("rent-2027-01", next.id());
        assertEquals(LocalDate.of(2027, 1, 1), next.dueDate());
    }

    @Test
    void endOfMonthDoesNotDrift() {
        scheduler.scheduleMonthly("saas", "Cloud", 99_00, 31, YearMonth.of(2027, 1));
        // Jan 31 2027 is a Sunday, so it's paid on Friday Jan 29
        assertEquals(List.of("saas-2027-01"),
                runner.run(LocalDate.of(2027, 1, 29)).stream().map(Payment::billId).toList());
        assertEquals(LocalDate.of(2027, 2, 28), only().dueDate());
        runner.run(LocalDate.of(2027, 2, 26));             // Feb 28 2027 is a Sunday -> Fri Feb 26
        assertEquals(LocalDate.of(2027, 3, 31), only().dueDate());
    }

    @Test
    void invalidDayOfMonthIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> scheduler.scheduleMonthly("x", "X", 100, 0, YearMonth.of(2026, 12)));
        assertThrows(IllegalArgumentException.class,
                () -> scheduler.scheduleMonthly("x", "X", 100, 32, YearMonth.of(2026, 12)));
    }

    @Test
    void oneOffBillsDoNotRecur() {
        scheduler.schedule(new Bill("once", "Printer", 400_00, LocalDate.of(2026, 12, 1)));
        runner.run(LocalDate.of(2026, 12, 1));
        assertTrue(scheduler.unpaidBills().isEmpty());
    }
}
