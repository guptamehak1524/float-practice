package com.acme.cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyResetTest {

    private TestClock clock;
    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        CardRepository cards = new CardRepository();
        cards.save(new Card("card-1", "acme", 100_000, CardStatus.ACTIVE));   // $1,000 / month
        // 2026-11-30 23:30 in Toronto (EST, UTC-5) == 2026-12-01 04:30 UTC
        clock = new TestClock(Instant.parse("2026-12-01T04:30:00Z"));
        service = new AuthorizationService(cards, new SpendTracker(), clock);
    }

    private AuthorizationResult spend(MerchantCategory category, long cents) {
        return service.authorize(new AuthorizationRequest("card-1", "Merchant", category, cents));
    }

    @Test
    void lateNightTorontoPurchaseCountsTowardNovember() {
        assertTrue(spend(MerchantCategory.TRAVEL, 100_000).approved());   // uses all of November
        clock.advance(Duration.ofMinutes(20));                           // 23:50 Toronto, still Nov
        assertEquals(DeclineReason.MONTHLY_LIMIT_EXCEEDED, spend(MerchantCategory.TRAVEL, 1_00).reason());
    }

    @Test
    void newMonthInTorontoResetsTheMonthlyLimit() {
        assertTrue(spend(MerchantCategory.TRAVEL, 100_000).approved());
        clock.advance(Duration.ofHours(1));                              // 00:30 Dec 1 Toronto
        assertTrue(spend(MerchantCategory.TRAVEL, 100_000).approved());
    }

    @Test
    void categoryLimitsResetToo() {
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 50_00);
        assertTrue(spend(MerchantCategory.MEALS, 50_00).approved());
        assertFalse(spend(MerchantCategory.MEALS, 1_00).approved());
        clock.advance(Duration.ofHours(1));
        assertTrue(spend(MerchantCategory.MEALS, 50_00).approved());
    }

    @Test
    void categoryLimitSettingSurvivesTheMonthChange() {
        service.setCategoryLimit("card-1", MerchantCategory.MEALS, 50_00);
        clock.advance(Duration.ofDays(40));
        assertEquals(DeclineReason.CATEGORY_LIMIT_EXCEEDED, spend(MerchantCategory.MEALS, 60_00).reason());
    }
}
