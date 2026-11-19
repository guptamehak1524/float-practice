# Follow-up 2 — Big reports first, and a broken org chart

*The interviewer says:* "Two more things came up in the retro."

## New requirements

1. **Queue order.** `pendingFor(approverId)` now lists reports **over $5,000** (`> 500_000` cents)
   first, then the rest. Within each group, keep oldest first.
2. **Bad org data.** The HR sync once produced a loop (A manages B, B manages A). Make routing safe:
   - Never route a report to someone who has **already approved it**.
   - If walking up the chain would loop forever, or would come back to a previous approver,
     throw `IllegalStateException` whose message contains `cycle`.

## Tests

Copy `followups/tests/PriorityAndCycleTest.java` into `src/test/java/com/acme/approvals/`.
Everything earlier must stay green.

## Talk about (no code needed)

- Is a sorted stream on every call OK, or would you keep a `PriorityQueue` per approver? When does it matter?
- Should a cycle throw, or send the report to finance and alert someone? Which is better for users?
