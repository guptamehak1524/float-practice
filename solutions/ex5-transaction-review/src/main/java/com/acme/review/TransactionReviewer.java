package com.acme.review;

import java.time.DayOfWeek;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reference solution after TASK + both follow-ups.
 * Each policy check is a small rule; review() and reviewAll() share them.
 */
public class TransactionReviewer {

    /** A stateless policy check. Returns the reason text if the rule applies. */
    @FunctionalInterface
    interface Rule {
        Optional<String> check(Transaction t, CompanySettings s);

        static Rule when(java.util.function.BiPredicate<Transaction, CompanySettings> applies, String reason) {
            return (t, s) -> applies.test(t, s) ? Optional.of(reason) : Optional.empty();
        }
    }

    static final String GAMBLING_MCC = "7995";
    static final int HISTORY_SIZE = 5;
    static final long UNUSUAL_MULTIPLIER = 3;

    /** Order matters: it's the order reasons are reported in. */
    private static final List<Rule> DECLINE_RULES = List.of(
            Rule.when((t, s) -> s.blockedMerchants().contains(t.merchant().toLowerCase()), "merchant blocked"),
            Rule.when((t, s) -> s.blockedMccs().contains(t.mcc()), "mcc blocked"),
            Rule.when((t, s) -> t.amountCents() > s.maxSingleTransactionCents(), "over single transaction limit"),
            Rule.when((t, s) -> !s.allowInternational() && !"CA".equals(t.country()), "international not allowed"),
            Rule.when(TransactionReviewer::isBlockedWeekend, "weekend spend")
    );

    private static final List<Rule> FLAG_RULES = List.of(
            Rule.when((t, s) -> GAMBLING_MCC.equals(t.mcc()), "gambling"),
            Rule.when((t, s) -> t.amountCents() > s.receiptRequiredOverCents(), "receipt required")
    );

    /** Last few non-declined amounts per card, with a running sum so the average is O(1). */
    private static final class CardHistory {
        final Deque<Long> amounts = new ArrayDeque<>();
        long sum;

        void add(long amount) {
            amounts.addLast(amount);
            sum += amount;
            if (amounts.size() > HISTORY_SIZE) {
                sum -= amounts.pollFirst();
            }
        }

        boolean isUnusual(long amount) {
            // amount > 3 * average  <=>  amount * size > 3 * sum  (integer maths, no rounding)
            return amounts.size() >= HISTORY_SIZE && amount * amounts.size() > UNUSUAL_MULTIPLIER * sum;
        }
    }

    private final Map<String, CardHistory> historyByCard = new HashMap<>();

    /** Legacy string API. The mobile app parses this, so the format must not change. */
    public String review(Transaction t, CompanySettings s) {
        ReviewDecision d = reviewAll(t, s);
        return d.outcome() == Outcome.APPROVED ? "APPROVED" : d.outcome() + ": " + d.reasons().get(0);
    }

    public ReviewDecision reviewAll(Transaction t, CompanySettings s) {
        if (t.amountCents() <= 0) {
            return new ReviewDecision(Outcome.DECLINED, List.of("invalid amount"));
        }
        List<String> declines = collect(DECLINE_RULES, t, s);
        List<String> flags = collect(FLAG_RULES, t, s);

        CardHistory history = historyByCard.computeIfAbsent(t.cardId(), k -> new CardHistory());
        if (history.isUnusual(t.amountCents())) {
            flags.add("unusual amount");
        }

        Outcome outcome = !declines.isEmpty() ? Outcome.DECLINED : !flags.isEmpty() ? Outcome.FLAGGED : Outcome.APPROVED;
        if (outcome != Outcome.DECLINED) {
            history.add(t.amountCents());
        }

        List<String> reasons = new ArrayList<>(declines);
        reasons.addAll(flags);
        return new ReviewDecision(outcome, List.copyOf(reasons));
    }

    private static List<String> collect(List<Rule> rules, Transaction t, CompanySettings s) {
        List<String> out = new ArrayList<>();
        for (Rule r : rules) {
            r.check(t, s).ifPresent(out::add);
        }
        return out;
    }

    private static boolean isBlockedWeekend(Transaction t, CompanySettings s) {
        if (!s.blockWeekends()) {
            return false;
        }
        DayOfWeek day = t.at().atZone(s.zone()).getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
