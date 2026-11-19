package com.acme.approvals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory org chart. In production this is backed by the HR integration. */
public class EmployeeDirectory {

    private final Map<String, Employee> employees = new HashMap<>();

    public void add(Employee employee) {
        employees.put(employee.id(), employee);
    }

    public Optional<Employee> find(String id) {
        return Optional.ofNullable(employees.get(id));
    }

    /** The direct manager of the given employee, if they have one. */
    public Optional<Employee> managerOf(String employeeId) {
        Employee e = employees.get(employeeId);
        if (e == null || e.managerId() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(employees.get(e.managerId()));
    }
}
