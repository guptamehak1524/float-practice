package com.acme.receipts;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the new feature in TASK.md. These fail until you implement it. */
class MatchingRulesTest {

    private static final LocalDate NOV_9 = LocalDate.of(2026, 11, 9);
    private static final LocalDate NOV_10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate NOV_11 = LocalDate.of(2026, 11, 11);
    private static final LocalDate NOV_12 = LocalDate.of(2026, 11, 12);
    private final ReceiptMatcher matcher = new ReceiptMatcher();

    private static CardTransaction txn(String id, String merchant, long cents, LocalDate d) {
        return new CardTransaction(id, "ana", merchant, cents, d);
    }

    private static Receipt receipt(String id, String merchant, long cents, LocalDate d) {
        return new Receipt(id, "ana", merchant, cents, d);
    }

    @Test
    void transactionPostedNextDayStillMatches() {
        MatchResult r = matcher.match(List.of(txn("t1", "STARBUCKS #4521 TORONTO ON", 6_45, NOV_11)),
                                      List.of(receipt("r1", "Starbucks", 6_45, NOV_10)));
        assertEquals(List.of(new Match("t1", "r1")), r.matches());
    }

    @Test
    void transactionOneDayEarlierStillMatches() {
        MatchResult r = matcher.match(List.of(txn("t1", "STARBUCKS #4521 TORONTO ON", 6_45, NOV_9)),
                                      List.of(receipt("r1", "Starbucks", 6_45, NOV_10)));
        assertEquals(List.of(new Match("t1", "r1")), r.matches());
    }

    @Test
    void twoDaysApartDoesNotMatch() {
        MatchResult r = matcher.match(List.of(txn("t1", "STARBUCKS #4521 TORONTO ON", 6_45, NOV_12)),
                                      List.of(receipt("r1", "Starbucks", 6_45, NOV_10)));
        assertTrue(r.matches().isEmpty());
    }

    @Test
    void sameDayIsPreferredOverNeighbouringDays() {
        MatchResult r = matcher.match(
                List.of(txn("t1", "UBER *TRIP HELP.UBER.COM", 18_20, NOV_11),
                        txn("t2", "UBER *TRIP HELP.UBER.COM", 18_20, NOV_10)),
                List.of(receipt("r1", "Uber", 18_20, NOV_10)));
        assertEquals(List.of(new Match("t2", "r1")), r.matches());
        assertEquals(List.of("t1"), r.unmatchedTransactionIds());
    }

    @Test
    void dayAfterIsPreferredOverDayBefore() {
        MatchResult r = matcher.match(
                List.of(txn("t1", "UBER *TRIP HELP.UBER.COM", 18_20, NOV_9),
                        txn("t2", "UBER *TRIP HELP.UBER.COM", 18_20, NOV_11)),
                List.of(receipt("r1", "Uber", 18_20, NOV_10)));
        assertEquals(List.of(new Match("t2", "r1")), r.matches());
    }

    @Test
    void merchantNamesMustLooselyAgree() {
        MatchResult r = matcher.match(
                List.of(txn("t1", "STARBUCKS #4521 TORONTO ON", 6_45, NOV_10)),
                List.of(receipt("r1", "Tim Hortons", 6_45, NOV_10)));
        assertTrue(r.matches().isEmpty(), "same amount and day, but a different merchant");
    }

    @Test
    void merchantComparisonIgnoresCaseDigitsAndPunctuation() {
        MatchResult r = matcher.match(
                List.of(txn("t1", "AMZN Mktp CA*2K4", 39_99, NOV_10)),
                List.of(receipt("r1", "amzn mktp", 39_99, NOV_10)));
        assertEquals(List.of(new Match("t1", "r1")), r.matches());
    }

    @Test
    void fiftyThousandOfEachRunsUnderTwoSeconds() {
        List<CardTransaction> txns = new ArrayList<>();
        List<Receipt> receipts = new ArrayList<>();
        LocalDate base = LocalDate.of(2026, 10, 1);
        for (int i = 0; i < 50_000; i++) {
            txns.add(new CardTransaction("t" + i, "e" + (i % 200), "VENDOR " + (i % 50) + " INC", 1_000 + i, base.plusDays(i % 20)));
        }
        for (int i = 49_999; i >= 0; i--) {
            receipts.add(new Receipt("r" + i, "e" + (i % 200), "Vendor", 1_000 + i, base.plusDays(i % 20 + 1)));
        }
        for (int i = 0; i < 10_000; i++) {
            receipts.add(new Receipt("x" + i, "nobody", "Vendor", 5, base));
        }
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            MatchResult r = matcher.match(txns, receipts);
            assertEquals(50_000, r.matches().size());
            assertEquals(10_000, r.unmatchedReceiptIds().size());
        });
    }
}
