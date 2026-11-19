# Follow-up 1 — Tips

*The interviewer says:* "Restaurant receipts don't match, because the card was charged with the tip
added, after the receipt was printed."

## New requirements

1. After all **exact-amount** matches are made (for every receipt, as before), make a second pass for
   receipts that are still unmatched.
2. In the second pass, a receipt may match a transaction (same employee, merchant rule, same
   date window) whose amount is **more than** the receipt amount and **at most 20% more**
   (`txn <= receipt * 1.20`, in whole cents, with no floating point).
3. If several qualify, pick the **smallest** amount difference, then the date preference
   (same day, day after, day before), then input order.
4. Exact matches always take priority: a transaction one receipt could use as a "tip match" must
   stay free if another receipt matches it exactly.

## Tests

Copy `followups/tests/TipMatchingTest.java` into `src/test/java/com/acme/receipts/`.

## Talk about (no code needed)

- Which structure gives you "all transactions for this employee with amount in [x, y]" without
  scanning? (Hint: a sorted map.)
- 20% is a guess. How would you pick the number with data?

Done? Open `FOLLOWUP-2.md`.
