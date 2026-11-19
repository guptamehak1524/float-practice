# Follow-up 2 — "Missing receipt" reminders

*The interviewer says:* "Now let's nudge people who haven't uploaded receipts."

## New requirements

Add to `ReceiptMatcher`:

```java
public Map<String, List<CardTransaction>> missingReceipts(List<CardTransaction> transactions,
                                                          MatchResult result,
                                                          LocalDate asOf,
                                                          int minAgeDays)
```

1. Include unmatched transactions that are at least `minAgeDays` old as of `asOf`
   (a transaction dated exactly `minAgeDays` days ago counts).
2. Group them by employee. Inside each group, oldest first, then by transaction id.
3. Order the groups (the map's iteration order) by the employee's **total missing amount, largest
   first**, then by employee id. Return a map that keeps that order.
4. Employees with nothing missing don't appear. `minAgeDays < 0` → `IllegalArgumentException`.

## Tests

Copy `followups/tests/MissingReceiptsTest.java` into `src/test/java/com/acme/receipts/`.

## Talk about (no code needed)

- Why did you need a `LinkedHashMap` (or `TreeMap` with a comparator), and not a plain `HashMap`?
- Streams with `groupingBy` vs a loop: which reads better to you here, and why?
