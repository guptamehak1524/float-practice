package com.acme.receipts;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Existing behaviour. These all pass today and must keep passing. */
class ReceiptMatcherTest {

    private static final LocalDate NOV_10 = LocalDate.of(2026, 11, 10);
    private final ReceiptMatcher matcher = new ReceiptMatcher();

    @Test
    void exactMatchOnEmployeeAmountAndDate() {
        MatchResult r = matcher.match(
                List.of(new CardTransaction("t1", "ana", "STAPLES 123", 45_99, NOV_10)),
                List.of(new Receipt("r1", "ana", "Staples", 45_99, NOV_10)));
        assertEquals(List.of(new Match("t1", "r1")), r.matches());
        assertTrue(r.unmatchedTransactionIds().isEmpty());
        assertTrue(r.unmatchedReceiptIds().isEmpty());
    }

    @Test
    void employeeMustMatch() {
        MatchResult r = matcher.match(
                List.of(new CardTransaction("t1", "ana", "STAPLES 123", 45_99, NOV_10)),
                List.of(new Receipt("r1", "ben", "Staples", 45_99, NOV_10)));
        assertTrue(r.matches().isEmpty());
        assertEquals(List.of("t1"), r.unmatchedTransactionIds());
        assertEquals(List.of("r1"), r.unmatchedReceiptIds());
    }

    @Test
    void identicalReceiptsPairOneToOne() {
        MatchResult r = matcher.match(
                List.of(new CardTransaction("t1", "ana", "TIM HORTONS 88", 3_25, NOV_10),
                        new CardTransaction("t2", "ana", "TIM HORTONS 88", 3_25, NOV_10)),
                List.of(new Receipt("r1", "ana", "Tim Hortons", 3_25, NOV_10),
                        new Receipt("r2", "ana", "Tim Hortons", 3_25, NOV_10),
                        new Receipt("r3", "ana", "Tim Hortons", 3_25, NOV_10)));
        assertEquals(List.of(new Match("t1", "r1"), new Match("t2", "r2")), r.matches());
        assertEquals(List.of("r3"), r.unmatchedReceiptIds());
    }

    @Test
    void unmatchedAreReportedInInputOrder() {
        MatchResult r = matcher.match(
                List.of(new CardTransaction("t3", "ana", "A", 1_00, NOV_10),
                        new CardTransaction("t1", "ana", "B", 2_00, NOV_10)),
                List.of());
        assertEquals(List.of("t3", "t1"), r.unmatchedTransactionIds());
    }

    @Test
    void emptyInputs() {
        MatchResult r = matcher.match(List.of(), List.of());
        assertEquals(new MatchResult(List.of(), List.of(), List.of()), r);
    }
}
