package com.acme.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatementTest {

    private TestClock clock;
    private Ledger ledger;

    @BeforeEach
    void setUp() {
        clock = new TestClock(Instant.parse("2026-10-31T16:00:00Z"));    // Oct 31, noon Toronto (EDT, UTC-4)
        ledger = new Ledger(clock);
        ledger.openAccount(new Account("ops", "Operating", "CAD"));
        ledger.post("ops", 1_000_00, "Opening deposit");                     // Oct 31
        clock.advance(Duration.ofHours(10));                                // Nov 1 02:00 UTC = Oct 31 22:00 Toronto
        ledger.post("ops", -100_00, "Late Halloween order");                // still Oct 31 in Toronto
        clock.advance(Duration.ofHours(11));                                // Nov 1 13:00 UTC = 08:00 Toronto (EST after DST ends)
        ledger.post("ops", 500_00, "Customer payment");                      // Nov 1
        clock.advance(Duration.ofDays(1));
        ledger.post("ops", -250_00, "Software subscription");                // Nov 2
        clock.advance(Duration.ofDays(1));
        ledger.post("ops", -50_00, "Coffee");                                // Nov 3
    }

    @Test
    void statementUsesTorontoDatesAndRunningBalance() {
        Statement s = ledger.statement("ops", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 2));
        assertEquals(900_00, s.openingBalanceCents());
        assertEquals(List.of(
                new StatementLine(LocalDate.of(2026, 11, 1), "Customer payment", 500_00, 1_400_00),
                new StatementLine(LocalDate.of(2026, 11, 2), "Software subscription", -250_00, 1_150_00)
        ), s.lines());
        assertEquals(1_150_00, s.closingBalanceCents());
    }

    @Test
    void emptyRangeStillHasBalances() {
        Statement s = ledger.statement("ops", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));
        assertTrue(s.lines().isEmpty());
        assertEquals(1_100_00, s.openingBalanceCents());
        assertEquals(1_100_00, s.closingBalanceCents());
    }

    @Test
    void invalidRangeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ledger.statement("ops", LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 1)));
    }

    @Test
    void balanceIsFastOnALargeLedger() {
        ledger.openAccount(new Account("big", "Big", "CAD"));
        for (int i = 0; i < 200_000; i++) {
            ledger.post(i % 2 == 0 ? "big" : "ops", 1, "tick");
        }
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            long total = 0;
            for (int i = 0; i < 20_000; i++) {
                total += ledger.balance(i % 2 == 0 ? "big" : "ops");
            }
            assertTrue(total > 0);
        });
        assertEquals(100_000, ledger.balance("big"));
    }
}
