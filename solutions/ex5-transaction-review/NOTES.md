# Exercise 5 — reference solution notes

Changed files: `TransactionReviewer.java`, `CompanySettings.java`.

## How the solution thinks

- **Refactor first, under green tests.** The characterization tests pin the exact strings. The
  safe sequence was: extract each `if` into a rule → put the rules into ordered lists → build
  `reviewAll` from the lists → rewrite `review` as `"<OUTCOME>: " + reasons.get(0)`. Run the tests
  after each step.
- **Why the old priorities still hold:** the old nested `if/else` meant "first decline wins" and
  "gambling overwrites receipt". Ordering the decline list the old way, and putting `gambling` before
  `receipt required`, reproduces both quirks with `reasons.get(0)`.
- **`Rule` is a tiny functional interface** plus a `Rule.when(predicate, reason)` helper, so most
  rules are one line. Adding the weekend rule was one line in `DECLINE_RULES` plus two builder fields.
- **Normalise data where it enters** (the settings builder lower-cases blocked merchants), not in
  every rule. That fixes the case bug for good.
- **The stateful rule is kept apart from the stateless list.** `CardHistory` holds a `Deque` of the
  last 5 amounts and a running sum, so each update and each average is O(1). The comparison
  `amount * size > 3 * sum` avoids integer-division rounding.
- **Declined transactions aren't remembered:** history is updated only after the outcome is known.

## What to say in the interview

- "Before touching this I'll run the characterization tests. They're my safety net; I won't change
  behaviour and structure in the same step."
- "`review` now calls `reviewAll`, so there's one source of truth for the rules."
- "In production `CardHistory` would live in a cache or database keyed by card. Two servers reviewing
  the same card at once could race, which is fine for a soft flag but not for a hard decline."

## Common mistakes

- Rewriting the whole method in one go, then spending the rest of the session chasing a broken string.
- Letting `invalid amount` run the other rules too (the test expects it alone).
- Putting `gambling` after `receipt required`, which breaks `gamblingWinsOverReceipt`.
- Calculating the average with integer division (`sum / 5`) and getting the 3× boundary wrong.
