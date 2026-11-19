package com.acme.ledger;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Append-only ledger of account entries. */
public class Ledger {

    private final Map<String, Account> accounts = new HashMap<>();
    private final List<LedgerEntry> entries = new ArrayList<>();
    private final Clock clock;
    private int nextEntryId = 1;

    public Ledger(Clock clock) {
        this.clock = clock;
    }

    public void openAccount(Account account) {
        if (accounts.containsKey(account.id())) {
            throw new IllegalArgumentException("Account already exists: " + account.id());
        }
        accounts.put(account.id(), account);
    }

    public Optional<Account> account(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public LedgerEntry post(String accountId, long amountCents, String description) {
        if (!accounts.containsKey(accountId)) {
            throw new IllegalArgumentException("Unknown account " + accountId);
        }
        LedgerEntry entry = new LedgerEntry("E-" + nextEntryId++, accountId, amountCents, description, clock.instant());
        entries.add(entry);
        return entry;
    }

    public long balance(String accountId) {
        long sum = 0;
        for (LedgerEntry e : entries) {
            if (e.accountId().equals(accountId)) {
                sum += e.amountCents();
            }
        }
        return sum;
    }

    /** All entries for an account, oldest first. */
    public List<LedgerEntry> entries(String accountId) {
        return entries.stream().filter(e -> e.accountId().equals(accountId)).toList();
    }

    Clock clock() {
        return clock;
    }
}
