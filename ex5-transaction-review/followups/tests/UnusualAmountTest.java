package com.acme.review;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.acme.review.TestData.WEDNESDAY_NOON_TORONTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UnusualAmountTest {

    private final TransactionReviewer reviewer = new TransactionReviewer();
    private final CompanySettings settings = CompanySettings.builder("acme").receiptRequiredOverCents(1_000_000_00).build();

    private ReviewDecision spend(String card, long cents) {
        return reviewer.reviewAll(TestData.txn(card, cents, "Merchant", "5999", "CA", WEDNESDAY_NOON_TORONTO), settings);
    }

    private void warmUp(String card, long... amounts) {
        for (long a : amounts) {
            assertEquals(Outcome.APPROVED, spend(card, a).outcome());
        }
    }

    @Test
    void spikeAfterFiveNormalPurchasesIsFlagged() {
        warmUp("card-1", 20_00, 20_00, 20_00, 20_00, 20_00);              // average $20
        assertEquals(new ReviewDecision(Outcome.FLAGGED, List.of("unusual amount")), spend("card-1", 61_00));
    }

    @Test
    void exactlyThreeTimesIsNotUnusual() {
        warmUp("card-1", 20_00, 20_00, 20_00, 20_00, 20_00);
        assertEquals(Outcome.APPROVED, spend("card-1", 60_00).outcome());
    }

    @Test
    void needsFiveTransactionsOfHistory() {
        warmUp("card-1", 10_00, 10_00, 10_00, 10_00);
        assertEquals(Outcome.APPROVED, spend("card-1", 500_00).outcome());
    }

    @Test
    void onlyTheLastFiveCount() {
        warmUp("card-1", 1_000_00, 10_00, 10_00, 10_00, 10_00, 10_00);   // the $1,000 has dropped out
        assertEquals(Outcome.FLAGGED, spend("card-1", 31_00).outcome());
    }

    @Test
    void cardsAreIndependent() {
        warmUp("card-1", 10_00, 10_00, 10_00, 10_00, 10_00);
        assertEquals(Outcome.APPROVED, spend("card-2", 900_00).outcome());
    }

    @Test
    void declinedTransactionsAreNotRemembered() {
        CompanySettings strict = CompanySettings.builder("acme")
                .maxSingleTransactionCents(100_00).receiptRequiredOverCents(1_000_000_00).build();
        for (int i = 0; i < 5; i++) {
            reviewer.reviewAll(TestData.txn("card-9", 10_00, "M", "5999", "CA", WEDNESDAY_NOON_TORONTO), strict);
        }
        reviewer.reviewAll(TestData.txn("card-9", 5_000_00, "M", "5999", "CA", WEDNESDAY_NOON_TORONTO), strict); // declined
        ReviewDecision d = reviewer.reviewAll(TestData.txn("card-9", 31_00, "M", "5999", "CA", WEDNESDAY_NOON_TORONTO), strict);
        assertEquals(List.of("unusual amount"), d.reasons());
    }
}
