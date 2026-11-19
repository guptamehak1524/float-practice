package com.acme.approvals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Existing behaviour. These all pass today and must keep passing. */
class ApprovalServiceTest {

    private TestClock clock;
    private ApprovalService service;

    @BeforeEach
    void setUp() {
        EmployeeDirectory directory = new EmployeeDirectory();
        directory.add(new Employee("ceo", "Cleo", null, 10_000_000));
        directory.add(new Employee("vp", "Victor", "ceo", 2_000_000));
        directory.add(new Employee("mgr", "Maya", "vp", 100_000));
        directory.add(new Employee("ana", "Ana", "mgr", 0));
        directory.add(new Employee("ben", "Ben", "mgr", 0));
        clock = new TestClock(Instant.parse("2026-11-02T14:00:00Z"));
        service = new ApprovalService(directory, new ReportRepository(), clock);
    }

    @Test
    void submitRoutesReportToDirectManager() {
        ExpenseReport r = service.submit("ana", 4_500, "Team lunch");
        assertEquals(ReportStatus.PENDING, r.status());
        assertEquals("mgr", r.currentApproverId());
    }

    @Test
    void submitRejectsNonPositiveAmounts() {
        assertThrows(IllegalArgumentException.class, () -> service.submit("ana", 0, "Nothing"));
        assertThrows(IllegalArgumentException.class, () -> service.submit("ana", -100, "Negative"));
    }

    @Test
    void submitRejectsUnknownEmployee() {
        assertThrows(IllegalArgumentException.class, () -> service.submit("ghost", 1_000, "?"));
    }

    @Test
    void employeeWithNoManagerCannotSubmit() {
        assertThrows(IllegalStateException.class, () -> service.submit("ceo", 1_000, "Coffee"));
    }

    @Test
    void assignedManagerCanApproveWithinTheirLimit() {
        ExpenseReport r = service.submit("ana", 4_500, "Team lunch");
        service.approve(r.id(), "mgr");
        assertEquals(ReportStatus.APPROVED, r.status());
        assertNull(r.currentApproverId());
    }

    @Test
    void someoneElseCannotApprove() {
        ExpenseReport r = service.submit("ana", 4_500, "Team lunch");
        assertThrows(NotAuthorizedException.class, () -> service.approve(r.id(), "vp"));
        assertEquals(ReportStatus.PENDING, r.status());
    }

    @Test
    void managerCanRejectWithReason() {
        ExpenseReport r = service.submit("ana", 4_500, "Team lunch");
        service.reject(r.id(), "mgr", "Missing receipt");
        assertEquals(ReportStatus.REJECTED, r.status());
        assertEquals("Missing receipt", r.rejectionReason());
    }

    @Test
    void cannotApproveTwice() {
        ExpenseReport r = service.submit("ana", 4_500, "Team lunch");
        service.approve(r.id(), "mgr");
        assertThrows(IllegalStateException.class, () -> service.approve(r.id(), "mgr"));
    }

    @Test
    void pendingForListsOldestFirst() {
        ExpenseReport first = service.submit("ana", 1_000, "Taxi");
        clock.advance(Duration.ofHours(1));
        ExpenseReport second = service.submit("ben", 2_000, "Parking");
        clock.advance(Duration.ofHours(1));
        ExpenseReport third = service.submit("ana", 3_000, "Lunch");

        List<String> ids = service.pendingFor("mgr").stream().map(ExpenseReport::id).toList();
        assertEquals(List.of(first.id(), second.id(), third.id()), ids);
    }
}
