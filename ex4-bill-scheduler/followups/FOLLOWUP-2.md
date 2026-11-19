# Follow-up 2 — Daily cash budget

*The interviewer says:* "Some customers don't keep enough cash in the account for every bill. If we
try to pay them all, the bank bounces payments and charges fees."

## New requirements

1. Add `List<Payment> run(LocalDate today, long budgetCents)`. The existing `run(today)` keeps
   working with no budget limit.
2. Go through payable bills in the usual order (earliest due, largest, id). Pay each one that still
   **fits in the remaining budget**. If one doesn't fit, **skip it and keep going**, since a
   smaller bill later might fit.
3. Skipped bills stay scheduled and are tried again on the next run.
4. A negative budget → `IllegalArgumentException`.

## Tests

Copy `followups/tests/BudgetTest.java` into `src/test/java/com/acme/billpay/`.

## Talk about (no code needed)

- "Skip and keep going" is greedy. Is it fair to the customer whose rent got skipped for three
  small bills? What other rules could product choose?
- If you used a `PriorityQueue`, how did you put skipped bills back without losing them?
