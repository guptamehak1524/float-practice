package com.acme.receipts;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TipMatchingTest {

    private static final LocalDate NOV_10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate NOV_11 = LocalDate.of(2026, 11, 11);
    private final ReceiptMatcher matcher = new ReceiptMatcher();

    private static CardTransaction txn(String id, long cents, LocalDate d) {
        return new CardTransaction(id, "ana", "THE KEG STEAKHOUSE 042", cents, d);
    }

    private static Receipt receipt(String id, long cents, LocalDate d) {
        return new Receipt(id, "ana", "The Keg", cents, d);
    }

    @Test
    void chargeWithTipMatchesReceipt() {
        MatchResult r = matcher.match(List.of(txn("t1", 115_00, NOV_11)), List.of(receipt("r1", 100_00, NOV_10)));
        assertEquals(List.of(new Match("t1", "r1")), r.matches());
    }

    @Test
    void exactlyTwentyPercentIsAllowedButNotMore() {
        assertEquals(1, matcher.match(List.of(txn("t1", 120_00, NOV_10)), List.of(receipt("r1", 100_00, NOV_10))).matches().size());
        assertEquals(0, matcher.match(List.of(txn("t1", 120_01, NOV_10)), List.of(receipt("r1", 100_00, NOV_10))).matches().size());
    }

    @Test
    void lowerChargeNeverMatches() {
        assertTrue(matcher.match(List.of(txn("t1", 99_99, NOV_10)), List.of(receipt("r1", 100_00, NOV_10))).matches().isEmpty());
    }

    @Test
    void smallestTipWins() {
        MatchResult r = matcher.match(
                List.of(txn("t1", 118_00, NOV_10), txn("t2", 112_00, NOV_11)),
                List.of(receipt("r1", 100_00, NOV_10)));
        assertEquals(List.of(new Match("t2", "r1")), r.matches());
    }

    @Test
    void exactMatchesAreMadeBeforeTipMatches() {
        // r1 comes first and could tip-match t1, but r2 matches t1 exactly. r1 should then take t2.
        MatchResult r = matcher.match(
                List.of(txn("t1", 110_00, NOV_10), txn("t2", 105_00, NOV_10)),
                List.of(receipt("r1", 100_00, NOV_10), receipt("r2", 110_00, NOV_10)));
        assertTrue(r.matches().contains(new Match("t1", "r2")));
        assertTrue(r.matches().contains(new Match("t2", "r1")));
        assertEquals(List.of(new Match("t2", "r1"), new Match("t1", "r2")), r.matches(), "still in receipt order");
    }
}
