# Follow-up 2 — Flag unusual amounts (a rule with memory)

*The interviewer says:* "Finance wants a heads-up when a card suddenly spends way more than usual."

## New requirements

1. Flag `unusual amount` when a transaction's amount is **more than 3×** the average of that
   **card's previous 5 reviewed transactions**. Exactly 3× is fine.
2. Only flag once the card has at least 5 previous transactions in its history.
3. Transactions with outcome `DECLINED` are **not** added to the card's history; approved and
   flagged ones are. Only the 5 most recent count.
4. It's the last reason in the list (after `receipt required`). Cards are independent.
5. The same `TransactionReviewer` instance is reused across calls, so history lives in the reviewer.

## Tests

Copy `followups/tests/UnusualAmountTest.java` into `src/test/java/com/acme/review/`.

## Talk about (no code needed)

- Your other rules are stateless; this one isn't. How did that change your `Rule` interface, if at all?
- Which structure keeps "last 5 amounts + their sum" in O(1) per transaction?
- In production this history lives in a database or cache, not in memory. What's the risk if two
  servers review the same card at once?
