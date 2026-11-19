package com.acme.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Existing behaviour. These all pass today and must keep passing. */
class TransferServiceTest {

    private Ledger ledger;
    private TransferService service;

    @BeforeEach
    void setUp() {
        ledger = new Ledger(new TestClock(Instant.parse("2026-11-10T15:00:00Z")));
        ledger.openAccount(new Account("ops", "Operating", "CAD"));
        ledger.openAccount(new Account("payroll", "Payroll", "CAD"));
        ledger.post("ops", 10_000_00, "Initial funding");
        service = new TransferService(ledger);
    }

    @Test
    void transferMovesMoneyBetweenAccounts() {
        Transfer t = service.transfer("ops", "payroll", 2_500_00, "November payroll");
        assertEquals(7_500_00, ledger.balance("ops"));
        assertEquals(2_500_00, ledger.balance("payroll"));
        assertEquals("T-1", t.id());
        assertEquals(t, service.find("T-1").orElseThrow());
    }

    @Test
    void moneyIsNeverCreatedOrDestroyed() {
        service.transfer("ops", "payroll", 1_234_56, "a");
        service.transfer("payroll", "ops", 34_56, "b");
        assertEquals(10_000_00, ledger.balance("ops") + ledger.balance("payroll"));
    }

    @Test
    void insufficientFundsThrowsAndPostsNothing() {
        int before = ledger.entries("ops").size();
        assertThrows(InsufficientFundsException.class, () -> service.transfer("ops", "payroll", 10_000_01, "Too much"));
        assertEquals(before, ledger.entries("ops").size());
        assertEquals(0, ledger.entries("payroll").size());
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> service.transfer("ops", "payroll", 0, "zero"));
        assertThrows(IllegalArgumentException.class, () -> service.transfer("ops", "ops", 100, "self"));
        assertThrows(IllegalArgumentException.class, () -> service.transfer("ops", "nope", 100, "unknown"));
    }

    @Test
    void eachTransferPostsADebitAndACredit() {
        service.transfer("ops", "payroll", 500_00, "x");
        assertEquals(-500_00, ledger.entries("ops").get(1).amountCents());
        assertEquals(500_00, ledger.entries("payroll").get(0).amountCents());
    }
}
