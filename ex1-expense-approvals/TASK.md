# Exercise 1 — Expense approvals: multi-level escalation

**Suggested time:** 45–60 min · **Main skills:** reading an existing service, walking a tree (org chart), keeping history in a list, state transitions

## Context

`ApprovalService` lets employees submit expense reports. Each report goes to the submitter's
direct manager, who approves or rejects it. Every `Employee` already has an `approvalLimitCents`,
but nothing uses it yet, so a manager with a $1,000 limit can approve a $50,000 report today.

Start by reading `ApprovalServiceTest` (existing behaviour, all green), then the code it touches.

## Ticket FIN-482: Respect approval limits

> As a finance admin, I want large expenses to be approved by someone senior enough,
> so that managers can't sign off on amounts above their limit.

Acceptance criteria:

1. When the current approver approves a report and the amount is **within their limit**
   (`amountCents <= approvalLimitCents`), the report becomes `APPROVED` (as today).
2. When the amount is **above their limit**, their approval is recorded but the report stays
   `PENDING` and moves to **their** manager, who becomes the new current approver.
3. This repeats up the org chart until someone with a high enough limit approves.
4. If the approver at the top of the chain (no manager) approves and the amount is still above
   their limit, the report becomes `ESCALATED_TO_FINANCE` with no current approver.
5. `ExpenseReport.approvalHistory()` returns everyone who approved, in order.
6. A rejection at any level ends the flow (`REJECTED`), as today.
7. All existing tests keep passing.

`EscalationTest` covers these and is red right now.

## How to work

- Run the tests first: `mvn -q test -pl ex1-expense-approvals` (or run the test classes in your IDE).
- Make one failing test pass at a time; narrate as if someone is pairing with you.
- When `EscalationTest` is green, open `followups/FOLLOWUP-1.md`. Don't peek ahead.
- Reference solution: `solutions/ex1-expense-approvals/` (only after you've tried).
