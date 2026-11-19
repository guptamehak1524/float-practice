# Exercise 6 — reference solution notes

Changed file: `ReceiptMatcher.java`.

## How the solution thinks

- **Rules first, speed second.** Get the small tests green with the simple loop, then replace
  the loop with an index and keep them green. Say this plan out loud.
- **The index is a nested map:** `employee → amount → date → candidates`. A lookup returns only
  real candidates, so matching is about O(n + m) instead of O(n·m). Dates use exact keys because
  there are only three to try (`0, +1, -1`), in preference order.
- **The amount level is a `TreeMap`** so the tip follow-up becomes a range query:
  `subMap(receipt, false, receipt * 1.2, true)`, iterated smallest first, which is exactly
  "smallest difference wins". With a `HashMap` there you'd have to scan.
- **`boolean[] used` by input position** keeps one-to-one matching cheap. `Indexed` carries the
  position so "first in input order" stays true inside each bucket.
- **Two passes** make "exact always beats tip" a structural guarantee, not a tie-break you can
  get wrong. Results go into `Match[] matchByReceipt` so the output stays in receipt order whatever pass found them.
- **No floating point for 20%:** `floorDiv(amount * 120, 100)`.
- **Reminders:** sort first, then `groupingBy` (the lists keep the sorted order), then order the
  groups by total into a `LinkedHashMap`, because a plain `HashMap` has no defined order.

## What to say in the interview

- "The nested loop is O(n·m): 50k × 60k is 3 billion comparisons. Indexing by the fields that must
  be equal is the standard fix."
- "Normalising merchant names is heuristic. I'd log near-misses and tune it with real data,
  maybe add a merchant alias table later."
- "If a company has millions of rows, I'd run this per employee or per day so memory stays bounded."

## Common mistakes

- Trying date offsets in the wrong order (the tests check same day → day after → day before).
- Letting a tip match grab a transaction before another receipt's exact match is considered.
- Using `double` for the 20% check (`120_00 <= 100_00 * 1.2` can fail with floating point).
- Normalising with `\\W` (keeps digits and `_`), so `"AMZN Mktp CA*2K4"` doesn't line up.
