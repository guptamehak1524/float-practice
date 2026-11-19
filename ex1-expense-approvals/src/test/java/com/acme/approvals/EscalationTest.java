package com.acme.approvals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the new feature in TASK.md. These fail until you implement it. */
class EscalationTest {

    private ApprovalService service;

    @BeforeEach
    void setUp() {
        EmployeeDirectory directory = new EmployeeDirectory();
        directory.add(new Employee("ceo", "Cleo", null, 10_000_000));   // $100,000
        directory.add(new Employee("vp", "Victor", "ceo", 2_000_000));   // $20,000
        directory.add(new Employee("mgr", "Maya", "vp", 100_000));       // $1,000
        directory.add(new Employee("ana", "Ana", "mgr", 0));
        service = new ApprovalService(directory, new ReportRepository(), new TestClock(Instant.parse("2026-11-02T14:00:00Z")));
    }

    @Test
    void approvalWithinLimitIsFinalAndRecorded() {
        ExpenseReport r = service.submit("ana", 100_000, "Exactly at the manager's limit");
        service.approve(r.id(), "mgr");
        assertEquals(ReportStatus.APPROVED, r.status());
        assertEquals(List.of("mgr"), r.approvalHistory());
    }

    @Test
    void approvalAboveLimitMovesReportToNextManager() {
        ExpenseReport r = service.submit("ana", 150_000, "Conference ticket");
        service.approve(r.id(), "mgr");

        assertEquals(ReportStatus.PENDING, r.status());
        assertEquals("vp", r.currentApproverId());
        assertEquals(List.of("mgr"), r.approvalHistory());
        assertEquals(List.of(r.id()), service.pendingFor("vp").stream().map(ExpenseReport::id).toList());
        assertTrue(service.pendingFor("mgr").isEmpty());
    }

    @Test
    void largeReportClimbsTheWholeChain() {
        ExpenseReport r = service.submit("ana", 5_000_000, "Offsite venue");
        service.approve(r.id(), "mgr");
        service.approve(r.id(), "vp");
        assertEquals("ceo", r.currentApproverId());
        service.approve(r.id(), "ceo");

        assertEquals(ReportStatus.APPROVED, r.status());
        assertEquals(List.of("mgr", "vp", "ceo"), r.approvalHistory());
    }

    @Test
    void previousApproverCannotActAgainAfterEscalation() {
        ExpenseReport r = service.submit("ana", 150_000, "Conference ticket");
        service.approve(r.id(), "mgr");
        assertThrows(NotAuthorizedException.class, () -> service.approve(r.id(), "mgr"));
    }

    @Test
    void reportNobodyCanApproveGoesToFinance() {
        ExpenseReport r = service.submit("ana", 25_000_000, "New office lease");   // $250,000
        service.approve(r.id(), "mgr");
        service.approve(r.id(), "vp");
        service.approve(r.id(), "ceo");

        assertEquals(ReportStatus.ESCALATED_TO_FINANCE, r.status());
        assertNull(r.currentApproverId());
        assertEquals(List.of("mgr", "vp", "ceo"), r.approvalHistory());
    }

    @Test
    void rejectionAtAnyLevelEndsTheFlow() {
        ExpenseReport r = service.submit("ana", 150_000, "Conference ticket");
        service.approve(r.id(), "mgr");
        service.reject(r.id(), "vp", "Not in budget");
        assertEquals(ReportStatus.REJECTED, r.status());
        assertEquals(List.of("mgr"), r.approvalHistory());
    }
}
