package com.acme.receipts;

import java.util.ArrayList;
import java.util.List;

/**
 * Pairs uploaded receipts with card transactions so employees don't have to do it by hand.
 * Runs nightly per company.
 */
public class ReceiptMatcher {

    public MatchResult match(List<CardTransaction> transactions, List<Receipt> receipts) {
        boolean[] used = new boolean[transactions.size()];
        List<Match> matches = new ArrayList<>();
        List<String> unmatchedReceipts = new ArrayList<>();

        for (Receipt r : receipts) {
            boolean found = false;
            for (int i = 0; i < transactions.size(); i++) {
                CardTransaction t = transactions.get(i);
                if (!used[i]
                        && t.employeeId().equals(r.employeeId())
                        && t.amountCents() == r.amountCents()
                        && t.date().equals(r.date())) {
                    used[i] = true;
                    matches.add(new Match(t.id(), r.id()));
                    found = true;
                    break;
                }
            }
            if (!found) {
                unmatchedReceipts.add(r.id());
            }
        }

        List<String> unmatchedTransactions = new ArrayList<>();
        for (int i = 0; i < transactions.size(); i++) {
            if (!used[i]) {
                unmatchedTransactions.add(transactions.get(i).id());
            }
        }
        return new MatchResult(matches, unmatchedTransactions, unmatchedReceipts);
    }
}
