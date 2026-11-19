# Exercise 5 — Transaction review: refactor a legacy method, then extend it

**Suggested time:** 50–60 min · **Main skills:** refactoring safely under characterization tests, strategy pattern / `List<Rule>`, keeping a legacy API stable

This is the "Gilded Rose" style of round: messy code, good tests, a new requirement that's painful
to add without cleaning up first.

## Context

`TransactionReviewer.review` decides whether a card transaction is approved, flagged or declined,
and returns a string like `"DECLINED: merchant blocked"`. The mobile app parses these strings,
so **their exact text must not change**. `TransactionReviewerTest` pins today's behaviour, quirks included.

Read the tests, then `review`. Notice it returns only **one** reason, even when several apply.

## Ticket FIN-560: Show every reason on the dashboard

> The web dashboard wants to show admins every rule a transaction broke, not just the first one.
> Also, the policy team added "blocked MCCs" to `CompanySettings` but it isn't enforced.

Acceptance criteria:

1. Implement `ReviewDecision reviewAll(Transaction, CompanySettings)`.
2. Reasons, in this fixed order, each included if it applies:
   - Declines: `invalid amount`, `merchant blocked`, `mcc blocked`, `over single transaction limit`, `international not allowed`
   - Flags: `gambling`, `receipt required`
3. `invalid amount` (amount ≤ 0) is the only reason reported for that transaction; no other rule runs.
4. Outcome: `DECLINED` if any decline reason; else `FLAGGED` if any flag; else `APPROVED` with no reasons.
5. **Enforce blocked MCCs** (`CompanySettings.blockedMccs()`).
6. `review(...)` keeps returning exactly the same strings as today for every existing test.
   For new cases it returns `"<OUTCOME>: <first reason>"`, or `"APPROVED"`.
7. Don't write the rules twice. `review` and `reviewAll` must share one implementation.

`ReviewAllTest` covers these and is red right now.

## How to work

- Run the characterization tests and keep them green after **every** small refactoring step.
- Suggested path: extract each `if` into a small rule → put the rules in a list → build
  `reviewAll` from the list → rewrite `review` on top of `reviewAll`.
- When `ReviewAllTest` is green, open `followups/FOLLOWUP-1.md`.
- Reference solution: `solutions/ex5-transaction-review/`.
