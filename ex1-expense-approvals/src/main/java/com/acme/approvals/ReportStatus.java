package com.acme.approvals;

public enum ReportStatus {
    PENDING,
    APPROVED,
    REJECTED,
    /** Reserved by the finance team for reports nobody in the org chart can approve. Not used yet. */
    ESCALATED_TO_FINANCE
}
