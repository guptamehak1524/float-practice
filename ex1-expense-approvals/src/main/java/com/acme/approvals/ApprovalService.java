package com.acme.approvals;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;

/**
 * Handles submitting expense reports and approving or rejecting them.
 * Today every report goes to the submitter's direct manager, who approves or rejects it.
 */
public class ApprovalService {

    private final EmployeeDirectory directory;
    private final ReportRepository repository;
    private final Clock clock;
    private int nextId = 1;

    public ApprovalService(EmployeeDirectory directory, ReportRepository repository, Clock clock) {
        this.directory = directory;
        this.repository = repository;
        this.clock = clock;
    }

    public ExpenseReport submit(String employeeId, long amountCents, String description) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Employee employee = directory.find(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown employee " + employeeId));
        Employee manager = directory.managerOf(employee.id())
                .orElseThrow(() -> new IllegalStateException(employeeId + " has no manager to approve this"));

        ExpenseReport report = new ExpenseReport("R-" + nextId++, employeeId, amountCents, description, clock.instant());
        report.setCurrentApproverId(manager.id());
        repository.save(report);
        return report;
    }

    public ExpenseReport approve(String reportId, String approverId) {
        ExpenseReport report = loadPendingReportFor(reportId, approverId);
        report.setStatus(ReportStatus.APPROVED);
        report.setCurrentApproverId(null);
        return report;
    }

    public ExpenseReport reject(String reportId, String approverId, String reason) {
        ExpenseReport report = loadPendingReportFor(reportId, approverId);
        report.setStatus(ReportStatus.REJECTED);
        report.setRejectionReason(reason);
        report.setCurrentApproverId(null);
        return report;
    }

    /** Reports waiting on this approver, oldest first. */
    public List<ExpenseReport> pendingFor(String approverId) {
        return repository.findAll().stream()
                .filter(r -> r.status() == ReportStatus.PENDING)
                .filter(r -> approverId.equals(r.currentApproverId()))
                .sorted(Comparator.comparing(ExpenseReport::submittedAt))
                .toList();
    }

    private ExpenseReport loadPendingReportFor(String reportId, String approverId) {
        ExpenseReport report = repository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown report " + reportId));
        if (report.status() != ReportStatus.PENDING) {
            throw new IllegalStateException("Report " + reportId + " is already " + report.status());
        }
        if (!approverId.equals(report.currentApproverId())) {
            throw new NotAuthorizedException(approverId + " cannot act on report " + reportId);
        }
        return report;
    }
}
