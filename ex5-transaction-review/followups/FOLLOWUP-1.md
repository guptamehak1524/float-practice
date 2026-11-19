# Follow-up 1 — Weekend rule, and a support bug

*The interviewer says:* "Your refactor is about to pay off."

## New requirements

1. **Weekend rule.** Some companies don't allow card spend on weekends. Add to the settings builder:
   - `blockWeekends(boolean)` (default `false`)
   - `zone(ZoneId)` (default `America/Toronto`)

   When enabled, a transaction whose `at` falls on a Saturday or Sunday **in the company's zone**
   is declined with reason `weekend spend`. It's the **last** decline reason, after
   `international not allowed`.
2. **Bug:** an admin typed `"Casino Royale"` in the blocked list, and it didn't block anything,
   because the code lowercases the merchant but not the list. Merchant blocking must be
   case-insensitive both ways.

## Tests

Copy `followups/tests/WeekendAndCaseTest.java` into `src/test/java/com/acme/review/`.

## Talk about (no code needed)

- How many lines of the old method would you have touched to add the weekend rule? How many now?
- Where should normalising the blocked list happen: in the builder, or in the rule? Why?

Done? Open `FOLLOWUP-2.md`.
