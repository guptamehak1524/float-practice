# Float coding round — practice kit (Java)

Six practice exercises built like the real round: **an existing codebase, an existing test suite,
and a ticket asking for a new feature**, followed by requirement changes that arrive one at a time.

## Setup (5 minutes)

- JDK 17 or newer, Maven 3.9+. Check with `java -version` and `mvn -version`.
- IntelliJ: *File → Open* → choose this folder's `pom.xml` → *Open as Project*. Each exercise
  is a module.
- The first build downloads JUnit 5 from Maven Central.

Run one exercise's tests:

```bash
mvn -q -pl ex1-expense-approvals test                        # all tests in the module
mvn -q -pl ex1-expense-approvals test -Dtest=EscalationTest  # one test class
```

When you start an exercise, the **existing tests pass and the new-feature tests fail**, so
`mvn test` reports failures. That's the starting point, like in the interview.

## How to do each exercise (mimics the 60-minute round)

1. Start a 60-minute timer. Talk out loud as if Nate is pairing with you.
2. Open `TASK.md`. Restate the ticket in your own words.
3. **Read the tests before the code.** Run them.
4. Make the feature tests pass one at a time. Refactor only when green.
5. Open `followups/FOLLOWUP-1.md` only when the ticket is done. Copy its test file from
   `followups/tests/` into the module's `src/test/java/...` folder and keep going. Then `FOLLOWUP-2.md`.
6. In the last 5 minutes, summarise what works, what's left, and what you'd do next.
7. Afterwards, compare with `solutions/<exercise>/`: the changed source files plus `NOTES.md`
   (the reasoning, what to say out loud, common mistakes).

No AI tools while practising, same as the real session. Looking up Java docs is fine.

## The exercises

| # | Module | Ticket | Data structures / skills it exercises |
| --- | --- | --- | --- |
| 1 | `ex1-expense-approvals` | Approval limits → escalate up the org chart | Tree walk, `List` history, cycle detection with `HashSet`, composed `Comparator` |
| 2 | `ex2-card-limits` | Per-category monthly card limits | `EnumMap`, nested maps, `YearMonth` + time zones, sliding window `Deque` |
| 3 | `ex3-ledger-transfers` | Idempotent transfers | `HashMap` idempotency, validate-then-write, append-only reversals, running sums |
| 4 | `ex4-bill-scheduler` | Weekend/holiday bills never paid | `java.time`, ordering rules, recurring dates without drift, greedy budget |
| 5 | `ex5-transaction-review` | Report every policy reason (legacy refactor) | Characterization tests, strategy / `List<Rule>`, stateful rule with a `Deque` |
| 6 | `ex6-receipt-matching` | Fuzzy matching at 50k scale | Replace O(n·m) with a map index, `TreeMap.subMap` range queries, `groupingBy` |

Suggested order: 1 → 3 → 2 → 4 → 6 → 5. Exercise 5 is the closest to "refactor a messy function,
then add a feature", which a past Float candidate described, so do it last, timed, as a dress rehearsal.

## Pasting into CoderPad

Each exercise is small (5–9 files). To try one in CoderPad's Java environment, paste the classes
and the tests. If the pad has a single file, drop the `package` lines and make the classes
non-public.

## Folder layout

```
ex1-expense-approvals/
  TASK.md                     <- start here
  src/main/java/...           <- the "existing codebase"
  src/test/java/...           <- existing tests (green) + feature tests (red)
  followups/FOLLOWUP-1.md     <- open only after the ticket is done
  followups/FOLLOWUP-2.md
  followups/tests/*.java      <- tests for each follow-up
solutions/ex1-expense-approvals/
  NOTES.md                    <- reasoning, what to say, common mistakes
  src/main/java/...           <- changed files after the ticket + both follow-ups
```

To check a reference solution: copy its `src/main` files over the exercise's, copy the follow-up
tests into `src/test`, and run `mvn test` for that module. Every test passes.
