package com.acme.approvals;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriorityAndCycleTest {

    @Test
    void bigReportsComeFirstThenOldestFirst() {
        EmployeeDirectory directory = new EmployeeDirectory();
        directory.add(new Employee("mgr", "Maya", null, 10_000_000));
        directory.add(new Employee("ana", "Ana", "mgr", 0));
        TestClock clock = new TestClock(Instant.parse("2026-11-02T14:00:00Z"));
        ApprovalService service = new ApprovalService(directory, new ReportRepository(), clock);

        ExpenseReport smallOld = service.submit("ana", 2_000, "Taxi");
        clock.advance(Duration.ofMinutes(5));
        ExpenseReport bigOld = service.submit("ana", 600_000, "Laptop");
        clock.advance(Duration.ofMinutes(5));
        ExpenseReport exactlyFiveK = service.submit("ana", 500_000, "Monitor");   // not "over" $5,000
        clock.advance(Duration.ofMinutes(5));
        ExpenseReport bigNew = service.submit("ana", 900_000, "Flights");

        List<String> ids = service.pendingFor("mgr").stream().map(ExpenseReport::id).toList();
        assertEquals(List.of(bigOld.id(), bigNew.id(), smallOld.id(), exactlyFiveK.id()), ids);
    }

    @Test
    void escalationThatLoopsBackThrows() {
        EmployeeDirectory directory = new EmployeeDirectory();
        directory.add(new Employee("a", "A", "b", 0));
        directory.add(new Employee("b", "B", "a", 0));
        directory.add(new Employee("x", "X", "a", 0));
        ApprovalService service = new ApprovalService(directory, new ReportRepository(), new TestClock(Instant.parse("2026-11-02T14:00:00Z")));

        ExpenseReport r = service.submit("x", 1_000, "Snacks");
        service.approve(r.id(), "a");                    // over a's limit -> goes to b
        assertEquals("b", r.currentApproverId());
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.approve(r.id(), "b"));
        assertTrue(ex.getMessage().toLowerCase().contains("cycle"));
    }

    @Test
    void walkingAChainThatLoopsForeverThrows() {
        EmployeeDirectory directory = new EmployeeDirectory();
        directory.add(new Employee("a", "A", "b", 0));
        directory.add(new Employee("b", "B", "a", 0));
        directory.add(new Employee("x", "X", "a", 0));
        directory.setOnLeave("a", true);
        directory.setOnLeave("b", true);
        ApprovalService service = new ApprovalService(directory, new ReportRepository(), new TestClock(Instant.parse("2026-11-02T14:00:00Z")));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.submit("x", 1_000, "Snacks"));
        assertTrue(ex.getMessage().toLowerCase().contains("cycle"));
    }
}
