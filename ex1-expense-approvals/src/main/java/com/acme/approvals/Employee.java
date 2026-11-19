package com.acme.approvals;

import java.util.Objects;

/**
 * An employee in the org chart.
 *
 * @param managerId          id of this employee's manager, or null for the top of the org (e.g. the CEO)
 * @param approvalLimitCents the largest expense this person may approve on their own
 */
public record Employee(String id, String name, String managerId, long approvalLimitCents) {
    public Employee {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        if (approvalLimitCents < 0) {
            throw new IllegalArgumentException("approvalLimitCents must be >= 0");
        }
    }
}
