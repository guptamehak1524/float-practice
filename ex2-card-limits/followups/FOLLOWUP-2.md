# Follow-up 2 — Velocity check (card testing fraud)

*The interviewer says:* "Fraudsters test stolen cards with lots of quick small charges at one merchant. Let's stop that."

## New requirements

1. Add `DeclineReason.TOO_MANY_ATTEMPTS`.
2. If a card already has **3 approved** authorizations at the **same merchant** within the last
   **60 seconds**, decline the next one there with `TOO_MANY_ATTEMPTS`.
   - An attempt exactly 60 seconds after the oldest of those three is allowed again.
   - Declined attempts don't count toward the 3.
   - Other merchants are not affected.
3. Run the velocity check after the card/active checks but **before** the spend limits.
4. Keep it fast: no scanning the whole history log on every authorization.

## Tests

Copy `followups/tests/VelocityTest.java` into `src/test/java/com/acme/cards/`.

## Talk about (no code needed)

- Which data structure did you use for "the last 60 seconds", and why is it O(1) amortised?
- Memory grows with every (card, merchant) pair. How would you clean that up?
- Should the thresholds (3, 60s) be per company? How would you make that configurable without
  touching `authorize` again?
