package com.acme.approvals;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory store for expense reports. Keeps insertion order. */
public class ReportRepository {

    private final Map<String, ExpenseReport> reports = new LinkedHashMap<>();

    public void save(ExpenseReport report) {
        reports.put(report.id(), report);
    }

    public Optional<ExpenseReport> findById(String id) {
        return Optional.ofNullable(reports.get(id));
    }

    public Collection<ExpenseReport> findAll() {
        return reports.values();
    }
}
