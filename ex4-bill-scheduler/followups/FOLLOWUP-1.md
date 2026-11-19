# Follow-up 1 — Recurring monthly bills

*The interviewer says:* "Most bills are monthly: rent, software, insurance. Customers don't want to
re-enter them every month."

## New requirements

1. Add to `BillScheduler`:

   ```java
   public void scheduleMonthly(String seriesId, String vendor, long amountCents, int dayOfMonth, YearMonth firstMonth)
   ```

   It schedules the first bill with id `seriesId + "-" + firstMonth` (e.g. `rent-2026-11`).
2. When a bill from a series is **paid**, the next month's bill is scheduled automatically
   (`rent-2026-12`, …).
3. `dayOfMonth` greater than the month's length uses the **last day** of that month, without drifting:
   a series on the 31st is due Jan 31, Feb 28, Mar 31 (not Mar 28).
4. Weekend and holiday rules from the ticket still apply to every instance.
5. `dayOfMonth` outside 1–31 → `IllegalArgumentException`.

## Tests

Copy `followups/tests/RecurringBillTest.java` into `src/test/java/com/acme/billpay/`.

## Talk about (no code needed)

- What should happen if a customer cancels a series while one bill is still unpaid?
- Why does storing the *intended* day of month avoid the "Mar 28" drift bug?

Done? Open `FOLLOWUP-2.md`.
