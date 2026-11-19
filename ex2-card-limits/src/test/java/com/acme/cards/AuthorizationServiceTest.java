package com.acme.cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Existing behaviour. These all pass today and must keep passing. */
class AuthorizationServiceTest {

    private CardRepository cards;
    private SpendTracker tracker;
    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        cards = new CardRepository();
        cards.save(new Card("card-1", "acme", 100_000, CardStatus.ACTIVE));   // $1,000 / month
        cards.save(new Card("card-frozen", "acme", 100_000, CardStatus.FROZEN));
        tracker = new SpendTracker();
        service = new AuthorizationService(cards, tracker, new TestClock(Instant.parse("2026-11-10T15:00:00Z")));
    }

    private AuthorizationResult spend(String card, long cents) {
        return service.authorize(new AuthorizationRequest(card, "Staples", MerchantCategory.OFFICE_SUPPLIES, cents));
    }

    @Test
    void approvesUnderTheLimitAndRecordsSpend() {
        AuthorizationResult r = spend("card-1", 25_00);
        assertTrue(r.approved());
        assertNull(r.reason());
        assertEquals(25_00, tracker.spent("card-1"));
    }

    @Test
    void approvesExactlyAtTheLimit() {
        assertTrue(spend("card-1", 100_000).approved());
    }

    @Test
    void declinesOverTheLimitAndDoesNotRecordSpend() {
        spend("card-1", 90_000);
        AuthorizationResult r = spend("card-1", 20_000);
        assertFalse(r.approved());
        assertEquals(DeclineReason.MONTHLY_LIMIT_EXCEEDED, r.reason());
        assertEquals(90_000, tracker.spent("card-1"));
    }

    @Test
    void declinesFrozenCard() {
        assertEquals(DeclineReason.CARD_NOT_ACTIVE, spend("card-frozen", 1_00).reason());
    }

    @Test
    void declinesUnknownCard() {
        assertEquals(DeclineReason.CARD_NOT_FOUND, spend("nope", 1_00).reason());
    }

    @Test
    void declinesZeroOrNegativeAmounts() {
        assertEquals(DeclineReason.INVALID_AMOUNT, spend("card-1", 0).reason());
        assertEquals(DeclineReason.INVALID_AMOUNT, spend("card-1", -500).reason());
    }

    @Test
    void historyLogsEveryAttemptInOrder() {
        spend("card-1", 10_00);
        spend("card-1", 999_999);
        assertEquals(2, service.history("card-1").size());
        assertTrue(service.history("card-1").get(0).result().approved());
        assertFalse(service.history("card-1").get(1).result().approved());
    }
}
