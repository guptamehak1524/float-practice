package com.acme.review;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.acme.review.TestData.txn;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Characterization tests: they pin down what review() returns TODAY, quirks included.
 * The mobile app depends on these exact strings. They must keep passing after any refactor.
 */
class TransactionReviewerTest {

    private final TransactionReviewer reviewer = new TransactionReviewer();
    private final CompanySettings defaults = CompanySettings.builder("acme").build();

    @Test
    void normalPurchaseIsApproved() {
        assertEquals("APPROVED", reviewer.review(txn(50_00, "Staples", "5943", "CA"), defaults));
    }

    @Test
    void zeroAmountIsDeclined() {
        assertEquals("DECLINED: invalid amount", reviewer.review(txn(0, "Staples", "5943", "CA"), defaults));
    }

    @Test
    void blockedMerchantIsDeclined() {
        CompanySettings s = CompanySettings.builder("acme").blockedMerchants(Set.of("casino royale")).build();
        assertEquals("DECLINED: merchant blocked", reviewer.review(txn(10_00, "Casino Royale", "5812", "CA"), s));
    }

    @Test
    void overSingleTransactionLimitIsDeclined() {
        CompanySettings s = CompanySettings.builder("acme").maxSingleTransactionCents(1_000_00).build();
        assertEquals("DECLINED: over single transaction limit", reviewer.review(txn(1_500_00, "Apple", "5732", "CA"), s));
    }

    @Test
    void blockedMerchantWinsOverLimit() {
        CompanySettings s = CompanySettings.builder("acme")
                .blockedMerchants(Set.of("apple")).maxSingleTransactionCents(1_000_00).build();
        assertEquals("DECLINED: merchant blocked", reviewer.review(txn(1_500_00, "Apple", "5732", "CA"), s));
    }

    @Test
    void internationalIsDeclinedWhenNotAllowed() {
        CompanySettings s = CompanySettings.builder("acme").allowInternational(false).build();
        assertEquals("DECLINED: international not allowed", reviewer.review(txn(20_00, "Uber", "4121", "US"), s));
    }

    @Test
    void largePurchaseNeedsReceipt() {
        assertEquals("FLAGGED: receipt required", reviewer.review(txn(80_00, "Staples", "5943", "CA"), defaults));
    }

    @Test
    void exactlyAtReceiptThresholdIsApproved() {
        assertEquals("APPROVED", reviewer.review(txn(75_00, "Staples", "5943", "CA"), defaults));
    }

    @Test
    void gamblingIsFlagged() {
        assertEquals("FLAGGED: gambling", reviewer.review(txn(10_00, "Lucky Spins", "7995", "CA"), defaults));
    }

    @Test
    void gamblingWinsOverReceipt() {
        assertEquals("FLAGGED: gambling", reviewer.review(txn(500_00, "Lucky Spins", "7995", "CA"), defaults));
    }
}
