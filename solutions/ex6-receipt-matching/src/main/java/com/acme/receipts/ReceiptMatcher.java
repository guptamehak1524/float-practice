package com.acme.receipts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Reference solution after TASK + both follow-ups. */
public class ReceiptMatcher {

    /** Preferred date offsets from the receipt date: same day, day after, day before. */
    private static final int[] DATE_OFFSETS = {0, 1, -1};
    private static final long MAX_TIP_PERCENT = 20;

    /** A transaction plus its position in the input, so "first in input order" is easy. */
    private record Indexed(int position, CardTransaction txn) {}

    /** employee -> amount (sorted, for tip ranges) -> date -> candidates in input order. */
    private static final class Index {
        final Map<String, TreeMap<Long, Map<LocalDate, List<Indexed>>>> byEmployee = new HashMap<>();

        Index(List<CardTransaction> txns) {
            for (int i = 0; i < txns.size(); i++) {
                CardTransaction t = txns.get(i);
                byEmployee.computeIfAbsent(t.employeeId(), k -> new TreeMap<>())
                          .computeIfAbsent(t.amountCents(), k -> new HashMap<>())
                          .computeIfAbsent(t.date(), k -> new ArrayList<>())
                          .add(new Indexed(i, t));
            }
        }

        NavigableMap<Long, Map<LocalDate, List<Indexed>>> amountsFor(String employeeId) {
            return byEmployee.getOrDefault(employeeId, new TreeMap<>());
        }
    }

    public MatchResult match(List<CardTransaction> transactions, List<Receipt> receipts) {
        Index index = new Index(transactions);
        boolean[] used = new boolean[transactions.size()];
        Match[] matchByReceipt = new Match[receipts.size()];

        // Pass 1: exact amounts, for every receipt.
        for (int r = 0; r < receipts.size(); r++) {
            Receipt receipt = receipts.get(r);
            Map<LocalDate, List<Indexed>> byDate = index.amountsFor(receipt.employeeId()).get(receipt.amountCents());
            Indexed hit = findByDate(byDate, receipt, used);
            if (hit != null) {
                used[hit.position()] = true;
                matchByReceipt[r] = new Match(hit.txn().id(), receipt.id());
            }
        }

        // Pass 2 (FOLLOWUP-1): tips. Charged amount in (receipt, receipt * 1.20], smallest difference first.
        for (int r = 0; r < receipts.size(); r++) {
            if (matchByReceipt[r] != null) {
                continue;
            }
            Receipt receipt = receipts.get(r);
            long max = Math.floorDiv(receipt.amountCents() * (100 + MAX_TIP_PERCENT), 100);
            for (Map<LocalDate, List<Indexed>> byDate
                    : index.amountsFor(receipt.employeeId()).subMap(receipt.amountCents(), false, max, true).values()) {
                Indexed hit = findByDate(byDate, receipt, used);
                if (hit != null) {
                    used[hit.position()] = true;
                    matchByReceipt[r] = new Match(hit.txn().id(), receipt.id());
                    break;
                }
            }
        }

        List<Match> matches = new ArrayList<>();
        List<String> unmatchedReceipts = new ArrayList<>();
        for (int r = 0; r < receipts.size(); r++) {
            if (matchByReceipt[r] != null) {
                matches.add(matchByReceipt[r]);
            } else {
                unmatchedReceipts.add(receipts.get(r).id());
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

    public Map<String, List<CardTransaction>> missingReceipts(List<CardTransaction> transactions, MatchResult result,
                                                              LocalDate asOf, int minAgeDays) {
        if (minAgeDays < 0) {
            throw new IllegalArgumentException("minAgeDays must be >= 0");
        }
        Set<String> unmatched = new HashSet<>(result.unmatchedTransactionIds());
        LocalDate cutoff = asOf.minusDays(minAgeDays);

        Map<String, List<CardTransaction>> byEmployee = transactions.stream()
                .filter(t -> unmatched.contains(t.id()))
                .filter(t -> !t.date().isAfter(cutoff))
                .sorted(Comparator.comparing(CardTransaction::date).thenComparing(CardTransaction::id))
                .collect(Collectors.groupingBy(CardTransaction::employeeId));      // lists keep the sorted order

        Comparator<Map.Entry<String, List<CardTransaction>>> biggestTotalFirst =
                Comparator.comparingLong((Map.Entry<String, List<CardTransaction>> e) -> total(e.getValue())).reversed()
                          .thenComparing(Map.Entry::getKey);

        Map<String, List<CardTransaction>> ordered = new LinkedHashMap<>();
        byEmployee.entrySet().stream().sorted(biggestTotalFirst).forEach(e -> ordered.put(e.getKey(), e.getValue()));
        return ordered;
    }

    private static long total(List<CardTransaction> txns) {
        return txns.stream().mapToLong(CardTransaction::amountCents).sum();
    }

    /** First unused candidate whose merchant agrees, trying same day, day after, day before. */
    private static Indexed findByDate(Map<LocalDate, List<Indexed>> byDate, Receipt receipt, boolean[] used) {
        if (byDate == null) {
            return null;
        }
        for (int offset : DATE_OFFSETS) {
            List<Indexed> candidates = byDate.get(receipt.date().plusDays(offset));
            if (candidates == null) {
                continue;
            }
            for (Indexed c : candidates) {
                if (!used[c.position()] && merchantsAgree(c.txn().merchant(), receipt.merchant())) {
                    return c;
                }
            }
        }
        return null;
    }

    static boolean merchantsAgree(String a, String b) {
        String x = normalise(a);
        String y = normalise(b);
        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }
        return x.startsWith(y) || y.startsWith(x);
    }

    static String normalise(String merchant) {
        return merchant.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}
