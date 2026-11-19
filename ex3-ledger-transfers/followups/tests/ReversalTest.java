package com.acme.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ReversalTest {

    private Ledger ledger;
    private TransferService service;

    @BeforeEach
    void setUp() {
        ledger = new Ledger(new TestClock(Instant.parse("2026-11-10T15:00:00Z")));
        ledger.openAccount(new Account("ops", "Operating", "CAD"));
        ledger.openAccount(new Account("vendor", "Vendor", "CAD"));
        ledger.post("ops", 10_000_00, "Initial funding");
        service = new TransferService(ledger);
    }

    @Test
    void reversalMovesMoneyBackWithANewTransfer() {
        Transfer original = service.transfer("ops", "vendor", 1_500_00, "Invoice 77");
        Transfer reversal = service.reverse(original.id(), "Wrong vendor");

        assertNotEquals(original.id(), reversal.id());
        assertEquals("vendor", reversal.fromAccountId());
        assertEquals("ops", reversal.toAccountId());
        assertEquals(1_500_00, reversal.amountCents());
        assertTrue(reversal.memo().contains(original.id()));
        assertTrue(reversal.memo().contains("Wrong vendor"));
        assertEquals(10_000_00, ledger.balance("ops"));
        assertEquals(0, ledger.balance("vendor"));
    }

    @Test
    void historyIsKeptNotEdited() {
        Transfer original = service.transfer("ops", "vendor", 1_500_00, "Invoice 77");
        service.reverse(original.id(), "Wrong vendor");
        assertEquals(3, ledger.entries("ops").size());     // funding, debit, reversal credit
        assertEquals(2, ledger.entries("vendor").size());  // credit, reversal debit
    }

    @Test
    void cannotReverseTwice() {
        Transfer original = service.transfer("ops", "vendor", 1_500_00, "Invoice 77");
        service.reverse(original.id(), "oops");
        assertThrows(IllegalStateException.class, () -> service.reverse(original.id(), "again"));
        assertEquals(10_000_00, ledger.balance("ops"));
    }

    @Test
    void cannotReverseAReversal() {
        Transfer original = service.transfer("ops", "vendor", 1_500_00, "Invoice 77");
        Transfer reversal = service.reverse(original.id(), "oops");
        assertThrows(IllegalStateException.class, () -> service.reverse(reversal.id(), "undo the undo"));
    }

    @Test
    void unknownTransferIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.reverse("T-404", "?"));
    }

    @Test
    void receiverMustStillHaveTheMoney() {
        ledger.openAccount(new Account("other", "Other", "CAD"));
        Transfer original = service.transfer("ops", "vendor", 1_500_00, "Invoice 77");
        service.transfer("vendor", "other", 1_000_00, "vendor spent it");
        assertThrows(InsufficientFundsException.class, () -> service.reverse(original.id(), "too late"));
        assertEquals(500_00, ledger.balance("vendor"));
        // and the failed attempt didn't mark it as reversed
        service.transfer("other", "vendor", 1_000_00, "returned");
        assertNotNull(service.reverse(original.id(), "now it works"));
    }
}
