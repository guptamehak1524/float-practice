package com.acme.cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the new feature in TASK.md. These fail until you implement it. */
class CategoryLimitTest {

    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        CardRepository cards = new CardRepository();
        cards.save(new Card("card-1", "acme", 100_000, CardStatus.ACTIVE));   // $1,000 / month overall
        service = new AuthorizationService(cards, new SpendTracker(), new TestClock(Instant.parse("2026-11-10T15:00:00Z")));
    }

    private AuthorizationResult spend(MerchantCategory category, long cents) {
        return service.authorize(new AuthorizationRequest("card-1", "Some merchant", category, cents));
    }

    @Test
    void categoryLimitDeclinesOnceReached() {
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 50_00);
        assertTrue(spend(MerchantCategory.MEALS, 30_00).approved());
        AuthorizationResult r = spend(MerchantCategory.MEALS, 25_00);
        assertFalse(r.approved());
        assertEquals(DeclineReason.CATEGORY_LIMIT_EXCEEDED, r.reason());
        assertTrue(spend(MerchantCategory.MEALS, 20_00).approved(), "exactly reaching the cap is fine");
    }

    @Test
    void otherCategoriesAreUnaffected() {
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 50_00);
        spend(MerchantCategory.MEALS, 50_00);
        assertTrue(spend(MerchantCategory.TRAVEL, 400_00).approved());
    }

    @Test
    void categorySpendStillCountsTowardTheMonthlyLimit() {
        service.setCategoryLimit("card-1", MerchantCategory.SOFTWARE, 900_00);
        assertTrue(spend(MerchantCategory.SOFTWARE, 800_00).approved());
        AuthorizationResult r = spend(MerchantCategory.TRAVEL, 300_00);
        assertEquals(DeclineReason.MONTHLY_LIMIT_EXCEEDED, r.reason());
    }

    @Test
    void monthlyLimitIsCheckedBeforeCategoryLimit() {
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 50_00);
        AuthorizationResult r = spend(MerchantCategory.MEALS, 2_000_00);   // breaks both
        assertEquals(DeclineReason.MONTHLY_LIMIT_EXCEEDED, r.reason());
    }

    @Test
    void declinedCategorySpendIsNotCounted() {
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 50_00);
        spend(MerchantCategory.MEALS, 60_00);                          // declined
        assertTrue(spend(MerchantCategory.MEALS, 50_00).approved());   // full cap still available
    }

    @Test
    void cardsWithoutCategoryLimitsBehaveAsBefore() {
        assertTrue(spend(MerchantCategory.MEALS, 900_00).approved());
    }

    @Test
    void settingALimitValidatesInput() {
        assertThrows(IllegalArgumentException.class,
                () -> service.setCategoryLimit("card-1", MerchantCategory.MEALS, -1));
        assertThrows(IllegalArgumentException.class,
                () -> service.setCategoryLimit("no-such-card", MerchantCategory.MEALS, 10_00));
    }

    @Test
    void limitCanBeChanged() {
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 10_00);
        assertFalse(spend(MerchantCategory.MEALS, 20_00).approved());
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 30_00);
        assertTrue(spend(MerchantCategory.MEALS, 20_00).approved());
    }
}
