package com.acme.review;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeekendAndCaseTest {

    private final TransactionReviewer reviewer = new TransactionReviewer();
    private static final Instant SAT_NOON_TORONTO = Instant.parse("2026-11-21T17:00:00Z");
    private static final Instant FRI_2330_TORONTO = Instant.parse("2026-11-21T04:30:00Z");   // already Saturday in UTC

    private Transaction at(Instant when) {
        return TestData.txn("card-1", 20_00, "Uber", "4121", "CA", when);
    }

    @Test
    void weekendSpendIsDeclinedWhenBlocked() {
        CompanySettings s = CompanySettings.builder("acme").blockWeekends(true).build();
        assertEquals(new ReviewDecision(Outcome.DECLINED, List.of("weekend spend")), reviewer.reviewAll(at(SAT_NOON_TORONTO), s));
        assertEquals("DECLINED: weekend spend", reviewer.review(at(SAT_NOON_TORONTO), s));
    }

    @Test
    void weekendIsJudgedInTheCompanyZone() {
        CompanySettings toronto = CompanySettings.builder("acme").blockWeekends(true).build();
        assertEquals(Outcome.APPROVED, reviewer.reviewAll(at(FRI_2330_TORONTO), toronto).outcome());

        CompanySettings london = CompanySettings.builder("acme-uk").blockWeekends(true).zone(ZoneId.of("Europe/London")).build();
        assertEquals(Outcome.DECLINED, reviewer.reviewAll(at(FRI_2330_TORONTO), london).outcome());
    }

    @Test
    void weekendsAllowedByDefault() {
        assertEquals(Outcome.APPROVED, reviewer.reviewAll(at(SAT_NOON_TORONTO), CompanySettings.builder("acme").build()).outcome());
    }

    @Test
    void weekendIsTheLastDeclineReason() {
        CompanySettings s = CompanySettings.builder("acme").blockWeekends(true).allowInternational(false).build();
        Transaction t = TestData.txn("card-1", 20_00, "Uber", "4121", "US", SAT_NOON_TORONTO);
        assertEquals(List.of("international not allowed", "weekend spend"), reviewer.reviewAll(t, s).reasons());
    }

    @Test
    void merchantBlockingIgnoresCaseOnBothSides() {
        CompanySettings s = CompanySettings.builder("acme").blockedMerchants(Set.of("Casino Royale")).build();
        assertEquals("DECLINED: merchant blocked", reviewer.review(TestData.txn(10_00, "CASINO ROYALE", "5812", "CA"), s));
        assertEquals("DECLINED: merchant blocked", reviewer.review(TestData.txn(10_00, "casino royale", "5812", "CA"), s));
    }
}
