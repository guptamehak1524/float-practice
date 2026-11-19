package com.acme.review;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.acme.review.TestData.txn;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests for the new feature in TASK.md. These fail until you implement it. */
class ReviewAllTest {

    private final TransactionReviewer reviewer = new TransactionReviewer();

    @Test
    void cleanTransactionIsApprovedWithNoReasons() {
        ReviewDecision d = reviewer.reviewAll(txn(50_00, "Staples", "5943", "CA"), CompanySettings.builder("acme").build());
        assertEquals(new ReviewDecision(Outcome.APPROVED, List.of()), d);
    }

    @Test
    void everyDeclineReasonIsReportedInOrder() {
        CompanySettings s = CompanySettings.builder("acme")
                .blockedMerchants(Set.of("apple")).maxSingleTransactionCents(1_000_00).allowInternational(false).build();
        ReviewDecision d = reviewer.reviewAll(txn(1_500_00, "Apple", "5732", "US"), s);
        assertEquals(Outcome.DECLINED, d.outcome());
        assertEquals(List.of("merchant blocked", "over single transaction limit", "international not allowed", "receipt required"),
                d.reasons());
    }

    @Test
    void flagsOnlyMeansFlagged() {
        ReviewDecision d = reviewer.reviewAll(txn(500_00, "Lucky Spins", "7995", "CA"), CompanySettings.builder("acme").build());
        assertEquals(new ReviewDecision(Outcome.FLAGGED, List.of("gambling", "receipt required")), d);
    }

    @Test
    void invalidAmountStopsEverythingElse() {
        CompanySettings s = CompanySettings.builder("acme").allowInternational(false).build();
        ReviewDecision d = reviewer.reviewAll(txn(-5_00, "Lucky Spins", "7995", "US"), s);
        assertEquals(new ReviewDecision(Outcome.DECLINED, List.of("invalid amount")), d);
    }

    @Test
    void blockedMccIsNowEnforced() {
        CompanySettings s = CompanySettings.builder("acme").blockedMccs(Set.of("5813")).build();   // bars
        Transaction t = txn(30_00, "The Pub", "5813", "CA");
        assertEquals(new ReviewDecision(Outcome.DECLINED, List.of("mcc blocked")), reviewer.reviewAll(t, s));
        assertEquals("DECLINED: mcc blocked", reviewer.review(t, s));
    }

    @Test
    void mccIsCheckedAfterMerchantAndBeforeLimit() {
        CompanySettings s = CompanySettings.builder("acme")
                .blockedMerchants(Set.of("the pub")).blockedMccs(Set.of("5813")).maxSingleTransactionCents(10_00).build();
        ReviewDecision d = reviewer.reviewAll(txn(30_00, "The Pub", "5813", "CA"), s);
        assertEquals(List.of("merchant blocked", "mcc blocked", "over single transaction limit"), d.reasons());
        assertEquals("DECLINED: merchant blocked", reviewer.review(txn(30_00, "The Pub", "5813", "CA"), s));
    }
}
