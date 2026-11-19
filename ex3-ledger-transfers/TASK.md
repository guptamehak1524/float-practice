# Exercise 3 — Ledger: idempotent transfers

**Suggested time:** 45–60 min · **Main skills:** `HashMap` for idempotency, validating before writing, invariants (money in = money out)

## Context

`Ledger` is an append-only list of entries (credits positive, debits negative).
`TransferService.transfer(from, to, amount, memo)` checks the balance, then posts a debit and a credit.

Read `TransferServiceTest`, then `TransferService` and `Ledger`.

## Ticket FIN-530: Safe retries for transfers

> Our mobile app retries a transfer when the network times out. Last week a customer's payroll
> transfer went through twice. Make transfers safe to retry.

Acceptance criteria:

1. Implement `transfer(idempotencyKey, from, to, amount, memo)` (currently a stub).
2. The first call with a key performs the transfer. Any later call with the **same key and same
   request** returns the **same `Transfer`** and moves no money.
3. The same key with a **different** request (other accounts, amount or memo) throws `IllegalStateException`.
4. A call that **fails** (e.g. insufficient funds) does not use up the key; a later retry can succeed.
5. A blank or null key → `IllegalArgumentException`.
6. While you're in here, a bug: transfers between accounts in **different currencies** go through.
   Both `transfer` methods must reject them with `IllegalArgumentException` mentioning "currency".

`IdempotencyTest` covers these and is red right now.

## How to work

- Run the tests first. Notice how `transfer` validates before it posts anything. Keep that property.
- When `IdempotencyTest` is green, open `followups/FOLLOWUP-1.md`.
- Reference solution: `solutions/ex3-ledger-transfers/`.
