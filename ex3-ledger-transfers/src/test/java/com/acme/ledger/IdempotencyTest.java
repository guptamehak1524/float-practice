package com.acme.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the new feature in TASK.md. These fail until you implement it. */
class IdempotencyTest {

    private Ledger ledger;
    private TransferService service;

    @BeforeEach
    void setUp() {
        ledger = new Ledger(new TestClock(Instant.parse("2026-11-10T15:00:00Z")));
        ledger.openAccount(new Account("ops", "Operating", "CAD"));
        ledger.openAccount(new Account("payroll", "Payroll", "CAD"));
        ledger.openAccount(new Account("usd", "US operating", "USD"));
        ledger.post("ops", 10_000_00, "Initial funding");
        service = new TransferService(ledger);
    }

    @Test
    void retryWithSameKeyMovesMoneyOnce() {
        Transfer first = service.transfer("key-1", "ops", "payroll", 1_000_00, "Payroll");
        Transfer retry = service.transfer("key-1", "ops", "payroll", 1_000_00, "Payroll");
        assertEquals(first, retry);
        assertEquals(9_000_00, ledger.balance("ops"));
        assertEquals(1_000_00, ledger.balance("payroll"));
        assertEquals(1, ledger.entries("payroll").size());
    }

    @Test
    void differentKeysAreDifferentTransfers() {
        service.transfer("key-1", "ops", "payroll", 1_000_00, "a");
        service.transfer("key-2", "ops", "payroll", 1_000_00, "b");
        assertEquals(2_000_00, ledger.balance("payroll"));
    }

    @Test
    void reusingAKeyForADifferentRequestIsAnError() {
        service.transfer("key-1", "ops", "payroll", 1_000_00, "Payroll");
        assertThrows(IllegalStateException.class,
                () -> service.transfer("key-1", "ops", "payroll", 2_000_00, "Payroll"));
        assertEquals(1_000_00, ledger.balance("payroll"));
    }

    @Test
    void failedAttemptDoesNotUseUpTheKey() {
        assertThrows(InsufficientFundsException.class,
                () -> service.transfer("key-9", "payroll", "ops", 500_00, "Refund"));
        service.transfer("ops", "payroll", 500_00, "top up");
        Transfer ok = service.transfer("key-9", "payroll", "ops", 500_00, "Refund");
        assertNotNull(ok);
        assertEquals(0, ledger.balance("payroll"));
    }

    @Test
    void currenciesMustMatch() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transfer("key-1", "ops", "usd", 100_00, "FX"));
        assertTrue(ex.getMessage().toLowerCase().contains("currency"));
        assertEquals(10_000_00, ledger.balance("ops"));
    }

    @Test
    void oldMethodAlsoChecksCurrency() {
        assertThrows(IllegalArgumentException.class, () -> service.transfer("ops", "usd", 100_00, "FX"));
    }

    @Test
    void blankKeyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.transfer(" ", "ops", "payroll", 100_00, "x"));
    }
}
