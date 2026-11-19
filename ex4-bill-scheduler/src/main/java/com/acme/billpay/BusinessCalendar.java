package com.acme.billpay;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/** Bank business days: Monday to Friday, excluding bank holidays. */
public class BusinessCalendar {

    private final Set<LocalDate> holidays;

    public BusinessCalendar(Set<LocalDate> holidays) {
        this.holidays = Set.copyOf(holidays);
    }

    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY && !holidays.contains(date);
    }
}
