package com.acme.receipts;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MissingReceiptsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 30);
    private final ReceiptMatcher matcher = new ReceiptMatcher();

    private static CardTransaction t(String id, String emp, long cents, LocalDate d) {
        return new CardTransaction(id, emp, "SOME MERCHANT", cents, d);
    }

    @Test
    void groupsOldUnmatchedTransactionsByEmployeeBiggestTotalFirst() {
        List<CardTransaction> txns = List.of(
                t("t1", "ana", 50_00, TODAY.minusDays(10)),
                t("t2", "ben", 300_00, TODAY.minusDays(8)),
                t("t3", "ana", 20_00, TODAY.minusDays(15)),
                t("t4", "ana", 10_00, TODAY.minusDays(2)),       // too recent
                t("t5", "cy", 5_00, TODAY.minusDays(30)),
                t("t6", "ben", 1_00, TODAY.minusDays(7)));        // exactly 7 days: counts
        MatchResult result = matcher.match(txns, List.of());

        Map<String, List<CardTransaction>> missing = matcher.missingReceipts(txns, result, TODAY, 7);

        assertEquals(List.of("ben", "ana", "cy"), List.copyOf(missing.keySet()));
        assertEquals(List.of("t2", "t6"), missing.get("ben").stream().map(CardTransaction::id).toList());
        assertEquals(List.of("t3", "t1"), missing.get("ana").stream().map(CardTransaction::id).toList());
    }

    @Test
    void matchedTransactionsAreNotReminded() {
        List<CardTransaction> txns = List.of(t("t1", "ana", 50_00, TODAY.minusDays(10)));
        MatchResult result = matcher.match(txns, List.of(new Receipt("r1", "ana", "Some merchant", 50_00, TODAY.minusDays(10))));
        assertTrue(matcher.missingReceipts(txns, result, TODAY, 7).isEmpty());
    }

    @Test
    void tiesOnTotalAreBrokenByEmployeeId() {
        List<CardTransaction> txns = List.of(
                t("t1", "zoe", 10_00, TODAY.minusDays(9)),
                t("t2", "amy", 10_00, TODAY.minusDays(9)));
        Map<String, List<CardTransaction>> missing = matcher.missingReceipts(txns, matcher.match(txns, List.of()), TODAY, 7);
        assertEquals(List.of("amy", "zoe"), List.copyOf(missing.keySet()));
    }

    @Test
    void negativeAgeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> matcher.missingReceipts(List.of(), matcher.match(List.of(), List.of()), TODAY, -1));
    }
}
