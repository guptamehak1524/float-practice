# Exercise 4 — Bill pay: weekends, holidays and missed bills

**Suggested time:** 45–60 min · **Main skills:** dates with `java.time`, ordering with `Comparator` / `PriorityQueue`, finding a bug from a support ticket

## Context

Customers schedule vendor bills. `PaymentRunner.run(today)` runs once per **bank business day**
and pays `scheduler.billsDueOn(today)` through the `PaymentGateway`. There's a `BusinessCalendar`
class, but nothing uses it.

Read `PaymentRunnerTest`, then `PaymentRunner`, `BillScheduler` and `BusinessCalendar`.

## Ticket FIN-547: Bills due on weekends are never paid

> Customer: "My rent was due Sunday Nov 22 and it was never paid. Now I have a late fee."
>
> The runner only runs Mon–Fri, and it only pays bills due **exactly** today.

Acceptance criteria:

1. A bill due on a non-business day (weekend or holiday) is paid on the **previous business day**,
   so it's never late. Example: bills due Sat/Sun are paid on Friday; with Fri Dec 25 a holiday,
   bills due Dec 25 and Sat Dec 26 are paid on Thu Dec 24.
2. Any unpaid bill whose pay date has already passed (e.g. the job didn't run) is paid on the next run.
3. Pay order: earliest due date first, then largest amount, then bill id (so results are deterministic).
4. `run` on a non-business day throws `IllegalArgumentException`.
5. `Payment.paidOn` is the day it was actually paid.
6. Existing tests keep passing.

`BusinessDayTest` covers these and is red right now.

## How to work

- Before coding, write down for yourself: "which bills should `run(date)` pay?" as one rule.
- When `BusinessDayTest` is green, open `followups/FOLLOWUP-1.md`.
- Reference solution: `solutions/ex4-bill-scheduler/`.
