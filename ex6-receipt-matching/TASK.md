# Exercise 6 — Receipt matching: real-world rules, at scale

**Suggested time:** 50–60 min · **Main skills:** replacing an O(n·m) nested loop with a `HashMap` index, string normalisation, tie-breaking rules

## Context

Employees photograph receipts; `ReceiptMatcher.match` pairs each receipt with a card transaction
so nobody has to do it by hand. Today it needs the same employee, amount **and exact date**, and it
compares every receipt with every transaction.

Read `ReceiptMatcherTest`, then `ReceiptMatcher`.

## Ticket FIN-575: Match rate is 41%, it should be 90%+

> Analysis of unmatched pairs shows two causes:
> - Card transactions often **post a day after** the purchase (or, with time zones, a day before).
> - Big customers time out: the nightly job takes hours with 50k receipts.
>
> Also, as soon as we loosen dates we'll get false matches between two merchants with the same
> amount, so check the merchant name too.

Acceptance criteria:

1. A receipt can match a transaction of the same employee and amount whose date is the **same day,
   the day after, or the day before** the receipt date, preferred in that order. If several fit
   on the same day, take the first in input order.
2. **Merchant names must loosely agree:** lower-case both, keep only the letters a–z, and one must
   start with the other. `"STARBUCKS #4521 TORONTO ON"` matches `"Starbucks"`;
   `"AMZN Mktp CA*2K4"` matches `"amzn mktp"`; it doesn't match `"Tim Hortons"`.
3. Each transaction and each receipt is used at most once. Receipts are processed in input order.
4. 50,000 transactions and 60,000 receipts must finish in **under 2 seconds**.
5. Output order rules in `MatchResult` stay the same; existing tests keep passing.

`MatchingRulesTest` covers these and is red right now.

## How to work

- Get the rules right first with the small tests, then make it fast. Say this plan out loud.
- Think about what to put in the map **key** so a lookup gives you only real candidates.
- When `MatchingRulesTest` is green, open `followups/FOLLOWUP-1.md`.
- Reference solution: `solutions/ex6-receipt-matching/`.
