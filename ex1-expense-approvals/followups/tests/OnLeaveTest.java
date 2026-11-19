package com.acme.approvals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OnLeaveTest {

    private EmployeeDirectory directory;
    private ApprovalService service;

    @BeforeEach
    void setUp() {
        directory = new EmployeeDirectory();
        directory.add(new Employee("ceo", "Cleo", null, 10_000_000));
        directory.add(new Employee("vp", "Victor", "ceo", 2_000_000));
        directory.add(new Employee("mgr", "Maya", "vp", 100_000));
        directory.add(new Employee("ana", "Ana", "mgr", 0));
        service = new ApprovalService(directory, new ReportRepository(), new TestClock(Instant.parse("2026-11-02T14:00:00Z")));
    }

    @Test
    void submitSkipsManagerOnLeave() {
        directory.setOnLeave("mgr", true);
        ExpenseReport r = service.submit("ana", 5_000, "Taxi");
        assertEquals("vp", r.currentApproverId());
    }

    @Test
    void escalationSkipsManagerOnLeave() {
        directory.setOnLeave("vp", true);
        ExpenseReport r = service.submit("ana", 150_000, "Conference");
        service.approve(r.id(), "mgr");
        assertEquals("ceo", r.currentApproverId());
        assertEquals(List.of("mgr"), r.approvalHistory());
    }

    @Test
    void everyoneOnLeaveSendsReportToFinance() {
        directory.setOnLeave("mgr", true);
        directory.setOnLeave("vp", true);
        directory.setOnLeave("ceo", true);
        ExpenseReport r = service.submit("ana", 5_000, "Taxi");
        assertEquals(ReportStatus.ESCALATED_TO_FINANCE, r.status());
        assertNull(r.currentApproverId());
    }

    @Test
    void managerBackFromLeaveGetsNewReports() {
        directory.setOnLeave("mgr", true);
        directory.setOnLeave("mgr", false);
        ExpenseReport r = service.submit("ana", 5_000, "Taxi");
        assertEquals("mgr", r.currentApproverId());
    }

    @Test
    void employeeWithNoManagerStillCannotSubmit() {
        assertThrows(IllegalStateException.class, () -> service.submit("ceo", 1_000, "Coffee"));
    }
}
