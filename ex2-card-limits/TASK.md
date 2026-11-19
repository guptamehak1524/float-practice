# Exercise 2 — Card authorizations: category limits

**Suggested time:** 45–60 min · **Main skills:** nested maps / `EnumMap`, ordering of checks, not mutating on decline

## Context

`AuthorizationService.authorize` runs every time a corporate card is used. It checks the card
exists, is active, and is under its monthly limit, then records the spend in `SpendTracker`.
Every attempt is logged.

Read `AuthorizationServiceTest` first, then `AuthorizationService` and `SpendTracker`.

## Ticket FIN-511: Category limits

> As a company admin, I want to cap how much a card can spend on a merchant category each month
> (e.g. $500 on MEALS), so that a travel card isn't used for team dinners.

Acceptance criteria:

1. `setCategoryLimit(cardId, category, limitCents)` sets or replaces the cap for that card + category.
   It throws `IllegalArgumentException` for an unknown card or a negative limit.
2. An authorization in a capped category is declined with `CATEGORY_LIMIT_EXCEEDED` if it would
   take that category's spend over the cap. Reaching the cap exactly is allowed.
3. Category spend also counts toward the card's overall monthly limit.
4. When both limits would be broken, report `MONTHLY_LIMIT_EXCEEDED` (the overall limit is checked first).
5. Declined attempts never count toward any limit.
6. Cards and categories without a cap behave exactly as today.

`CategoryLimitTest` covers these and is red right now (the method is a stub).

## How to work

- Run the tests first. Read before you write.
- Think about where category spend should live: in `SpendTracker`, or somewhere new? Say why.
- When `CategoryLimitTest` is green, open `followups/FOLLOWUP-1.md`.
- Reference solution: `solutions/ex2-card-limits/`.
