package com.acme.cards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VelocityTest {

    private TestClock clock;
    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        CardRepository cards = new CardRepository();
        cards.save(new Card("card-1", "acme", 1_000_000, CardStatus.ACTIVE));
        clock = new TestClock(Instant.parse("2026-11-10T15:00:00Z"));
        service = new AuthorizationService(cards, new SpendTracker(), clock);
    }

    private AuthorizationResult spend(String merchant, long cents) {
        return service.authorize(new AuthorizationRequest("card-1", merchant, MerchantCategory.OTHER, cents));
    }

    @Test
    void fourthQuickChargeAtSameMerchantIsDeclined() {
        assertTrue(spend("shady.example", 1_00).approved());
        clock.advance(Duration.ofSeconds(10));
        assertTrue(spend("shady.example", 1_00).approved());
        clock.advance(Duration.ofSeconds(10));
        assertTrue(spend("shady.example", 1_00).approved());
        clock.advance(Duration.ofSeconds(10));
        assertEquals(DeclineReason.TOO_MANY_ATTEMPTS, spend("shady.example", 1_00).reason());
    }

    @Test
    void windowSlidesAfterSixtySeconds() {
        spend("shady.example", 1_00);                  // t=0
        clock.advance(Duration.ofSeconds(10));
        spend("shady.example", 1_00);                  // t=10
        clock.advance(Duration.ofSeconds(10));
        spend("shady.example", 1_00);                  // t=20
        clock.advance(Duration.ofSeconds(40));         // t=60: the t=0 charge has left the window
        assertTrue(spend("shady.example", 1_00).approved());
        assertEquals(DeclineReason.TOO_MANY_ATTEMPTS, spend("shady.example", 1_00).reason());
    }

    @Test
    void otherMerchantsAreNotAffected() {
        spend("shady.example", 1_00);
        spend("shady.example", 1_00);
        spend("shady.example", 1_00);
        assertTrue(spend("legit.example", 1_00).approved());
    }

    @Test
    void declinedAttemptsDoNotCount() {
        spend("shady.example", 1_00);
        spend("shady.example", 1_00);
        spend("shady.example", 99_999_999);            // declined for limit, not counted
        assertTrue(spend("shady.example", 1_00).approved());
    }

    @Test
    void velocityIsCheckedBeforeLimits() {
        spend("shady.example", 1_00);
        spend("shady.example", 1_00);
        spend("shady.example", 1_00);
        assertEquals(DeclineReason.TOO_MANY_ATTEMPTS, spend("shady.example", 99_999_999).reason());
    }
}
