package com.acme.billpay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the new feature in TASK.md. These fail until you implement it. */
class BusinessDayTest {

    private static final LocalDate TUE = LocalDate.of(2026, 11, 17);
    private static final LocalDate WED = LocalDate.of(2026, 11, 18);
    private static final LocalDate FRI = LocalDate.of(2026, 11, 20);
    private static final LocalDate SAT = LocalDate.of(2026, 11, 21);
    private static final LocalDate SUN = LocalDate.of(2026, 11, 22);
    private static final LocalDate MON = LocalDate.of(2026, 11, 23);
    private static final LocalDate XMAS_EVE = LocalDate.of(2026, 12, 24);   // Thursday
    private static final LocalDate XMAS = LocalDate.of(2026, 12, 25);       // Friday, bank holiday
    private static final LocalDate BOXING_SAT = LocalDate.of(2026, 12, 26);

    private BillScheduler scheduler;
    private FakeGateway gateway;
    private PaymentRunner runner;

    @BeforeEach
    void setUp() {
        scheduler = new BillScheduler();
        gateway = new FakeGateway();
        runner = new PaymentRunner(scheduler, gateway, new BusinessCalendar(Set.of(XMAS)));
    }

    private List<String> ids(List<Payment> payments) {
        return payments.stream().map(Payment::billId).toList();
    }

    @Test
    void fridayRunPaysWeekendBillsEarly() {
        scheduler.schedule(new Bill("sun", "Rent", 3_000_00, SUN));
        scheduler.schedule(new Bill("sat", "Hydro", 150_00, SAT));
        scheduler.schedule(new Bill("fri", "Payroll", 20_000_00, FRI));
        scheduler.schedule(new Bill("mon", "Cleaning", 80_00, MON));

        assertEquals(List.of("fri", "sat", "sun"), ids(runner.run(FRI)));
        assertEquals(List.of("mon"), scheduler.unpaidBills().stream().map(Bill::id).toList());
    }

    @Test
    void overdueBillsAreCaughtUp() {
        scheduler.schedule(new Bill("late", "Hydro", 150_00, TUE));
        scheduler.schedule(new Bill("today", "Internet", 90_00, WED));
        assertEquals(List.of("late", "today"), ids(runner.run(WED)));
    }

    @Test
    void holidaysMoveThePaymentEarlierToo() {
        scheduler.schedule(new Bill("boxing", "Supplier", 500_00, BOXING_SAT));
        scheduler.schedule(new Bill("xmas", "Rent", 3_000_00, XMAS));
        assertEquals(List.of("xmas", "boxing"), ids(runner.run(XMAS_EVE)));
    }

    @Test
    void sameDueDatePaysLargestFirstThenById() {
        scheduler.schedule(new Bill("b", "B", 100_00, WED));
        scheduler.schedule(new Bill("c", "C", 900_00, WED));
        scheduler.schedule(new Bill("a", "A", 100_00, WED));
        assertEquals(List.of("c", "a", "b"), ids(runner.run(WED)));
    }

    @Test
    void runningOnANonBusinessDayIsAMistake() {
        assertThrows(IllegalArgumentException.class, () -> runner.run(SAT));
        assertThrows(IllegalArgumentException.class, () -> runner.run(XMAS));
    }

    @Test
    void paymentRecordsTheDayItWasActuallyPaid() {
        scheduler.schedule(new Bill("sun", "Rent", 3_000_00, SUN));
        assertEquals(FRI, runner.run(FRI).get(0).paidOn());
    }
}
