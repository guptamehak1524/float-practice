# Follow-up 1 — Managers on leave

*The interviewer says:* "Nice. Product just told us people go on leave a lot, and reports get stuck
in their queue. Can we route around them?"

## New requirements

1. Add `EmployeeDirectory.setOnLeave(String employeeId, boolean onLeave)`.
2. When a report is **submitted** or **escalated**, skip any manager who is on leave and go to the
   next one up the chain.
3. If everyone up the chain is on leave (or there is nobody), the report goes straight to
   `ESCALATED_TO_FINANCE`, with no current approver. Submitting no longer throws in that case
   when the employee does have managers; it still throws if the employee has **no** manager at all.
4. Someone coming back from leave (`setOnLeave(id, false)`) receives new reports again.

## Tests

Copy `followups/tests/OnLeaveTest.java` into `src/test/java/com/acme/approvals/` and make it pass.
All earlier tests must stay green.

## Talk about (no code needed)

- What should happen to reports **already** waiting on someone who just went on leave?
- Where would "on leave" really come from (HR sync, calendar), and how would you test that boundary?

Done? Open `FOLLOWUP-2.md`.
