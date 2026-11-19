# Follow-up 1 — Reversals

*The interviewer says:* "Ops sometimes sends money to the wrong account. They need an undo, but
auditors say we can never edit or delete ledger history."

## New requirements

1. Add `Transfer reverse(String transferId, String reason)` to `TransferService`.
2. A reversal is a **new** transfer in the opposite direction for the same amount. Its memo
   should mention the original transfer id and the reason. No existing entries change.
3. A transfer can be reversed **once**. A second attempt throws `IllegalStateException`.
4. A reversal can't itself be reversed (`IllegalStateException`).
5. Unknown transfer id → `IllegalArgumentException`.
6. If the receiving account no longer has the money, throw `InsufficientFundsException` and post nothing.

## Tests

Copy `followups/tests/ReversalTest.java` into `src/test/java/com/acme/ledger/`.

## Talk about (no code needed)

- Why is "append a correcting entry" better than deleting the original in a financial system?
- Should a reversal be allowed to overdraw the receiver? Who decides?

Done? Open `FOLLOWUP-2.md`.
