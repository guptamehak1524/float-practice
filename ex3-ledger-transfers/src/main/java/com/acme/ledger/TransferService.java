package com.acme.ledger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Moves money between two accounts by posting a debit and a matching credit. */
public class TransferService {

    private final Ledger ledger;
    private final Map<String, Transfer> transfers = new HashMap<>();
    private int nextTransferId = 1;

    public TransferService(Ledger ledger) {
        this.ledger = ledger;
    }

    public Transfer transfer(String fromId, String toId, long amountCents, String memo) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        ledger.account(fromId).orElseThrow(() -> new IllegalArgumentException("Unknown account " + fromId));
        ledger.account(toId).orElseThrow(() -> new IllegalArgumentException("Unknown account " + toId));

        long available = ledger.balance(fromId);
        if (available < amountCents) {
            throw new InsufficientFundsException(fromId, available, amountCents);
        }

        String id = "T-" + nextTransferId++;
        ledger.post(fromId, -amountCents, "Transfer " + id + " to " + toId + ": " + memo);
        ledger.post(toId, amountCents, "Transfer " + id + " from " + fromId + ": " + memo);
        Transfer t = new Transfer(id, fromId, toId, amountCents, memo, ledger.clock().instant());
        transfers.put(id, t);
        return t;
    }

    /**
     * Same as {@link #transfer(String, String, long, String)}, but safe to retry:
     * clients send the same key when a request times out and they aren't sure it went through.
     */
    public Transfer transfer(String idempotencyKey, String fromId, String toId, long amountCents, String memo) {
        throw new UnsupportedOperationException("TODO FIN-530: idempotent transfers");
    }

    public Optional<Transfer> find(String transferId) {
        return Optional.ofNullable(transfers.get(transferId));
    }
}
