# Follow-up 2 — Statements, and balances that don't slow down

*The interviewer says:* "Two asks from the accounting team, and one from on-call."

## New requirements

1. **Fast balances (on-call).** `Ledger.balance()` scans every entry ever posted, so it slows down
   as the ledger grows. It must be O(1). The test posts 200,000 entries, then calls `balance` 20,000 times within 2 seconds.
2. **Statements (accounting).** Add to `Ledger`:

   ```java
   public Statement statement(String accountId, LocalDate fromInclusive, LocalDate toInclusive)
   ```

   with two new records:

   ```java
   public record Statement(long openingBalanceCents, List<StatementLine> lines, long closingBalanceCents) {}
   public record StatementLine(LocalDate date, String description, long amountCents, long balanceAfterCents) {}
   ```

   - Dates are **Toronto dates** (`America/Toronto`), not UTC.
   - Opening balance = everything before `fromInclusive`. Lines are the entries in the range, oldest
     first, each with the running balance after it. Closing = opening + the lines.
   - `fromInclusive` after `toInclusive` → `IllegalArgumentException`.

## Tests

Copy `followups/tests/StatementTest.java` into `src/test/java/com/acme/ledger/`.

## Talk about (no code needed)

- You now keep a running balance **and** the entries. How do you make sure they never disagree?
- How would you make `statement` fast for an account with millions of entries (per-account lists,
  `TreeMap<LocalDate, …>` of daily totals, database index)?
