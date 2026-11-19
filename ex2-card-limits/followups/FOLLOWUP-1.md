# Follow-up 1 — Limits must reset every month

*The interviewer says:* "Support says a customer's card got declined on December 2nd. Can you take a look at `SpendTracker`?"

It never resets, so November's spend is still counted in December.

## New requirements

1. Monthly and category spend reset at the start of each **calendar month in Toronto time**
   (`America/Toronto`), the company's home time zone. Use the injected `Clock`, not
   `LocalDate.now()`.
2. A purchase at 11:30 PM on Nov 30 Toronto time (which is already Dec 1 in UTC) counts toward **November**.
3. Old months don't need to be kept forever, but nothing should break if they are.

## Tests

Copy `followups/tests/MonthlyResetTest.java` into `src/test/java/com/acme/cards/`.
Everything earlier must stay green.

## Talk about (no code needed)

- Why is "key the spend by month" usually safer than "run a reset job at midnight"?
- Should the time zone be per company? Where would it come from?

Done? Open `FOLLOWUP-2.md`.
