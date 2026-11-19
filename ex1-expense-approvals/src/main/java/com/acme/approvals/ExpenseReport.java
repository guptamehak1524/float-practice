package com.acme.approvals;

import java.time.Instant;
import java.util.List;

/** An expense report moving through approval. Mutable: status and approver change over time. */
public class ExpenseReport {

    private final String id;
    private final String employeeId;
    private final long amountCents;
    private final String description;
    private final Instant submittedAt;

    private ReportStatus status = ReportStatus.PENDING;
    private String currentApproverId;
    private String rejectionReason;

    public ExpenseReport(String id, String employeeId, long amountCents, String description, Instant submittedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.amountCents = amountCents;
        this.description = description;
        this.submittedAt = submittedAt;
    }

    public String id() { return id; }
    public String employeeId() { return employeeId; }
    public long amountCents() { return amountCents; }
    public String description() { return description; }
    public Instant submittedAt() { return submittedAt; }
    public ReportStatus status() { return status; }
    public String currentApproverId() { return currentApproverId; }
    public String rejectionReason() { return rejectionReason; }

    /** Ids of everyone who has approved this report so far, in order. */
    public List<String> approvalHistory() {
        // TODO(finance-team): we don't track approval history yet
        return List.of();
    }

    void setStatus(ReportStatus status) { this.status = status; }
    void setCurrentApproverId(String approverId) { this.currentApproverId = approverId; }
    void setRejectionReason(String reason) { this.rejectionReason = reason; }
}
