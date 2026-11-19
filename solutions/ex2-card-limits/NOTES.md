# Exercise 2 — reference solution notes

Changed files: `AuthorizationService.java`, `SpendTracker.java`, `DeclineReason.java`.

## How the solution thinks

- **Check everything, then mutate once.** All the decline paths return before any state changes.
  `spendTracker.record(...)` and the velocity deque update happen only on approval. That one rule
  satisfies "declined attempts don't count" for limits *and* velocity.
- **Category limits are `Map<cardId, EnumMap<MerchantCategory, Long>>`.** `EnumMap` is
  array-backed and ordered. A missing entry means "no cap", which keeps old behaviour.
- **Monthly reset = key spend by `YearMonth`**, computed from the clock in `America/Toronto`.
  There's no reset job to fail or run late, and tests just move the clock. The key is a private
  record (`CardMonth`), which gives correct `equals`/`hashCode` for free, unlike string concatenation.
- **Velocity = sliding window with a `Deque<Instant>` per (card, merchant)**: evict from the front
  while older than the window, check size, append on approval. Each timestamp is added and removed
  once, so it's O(1) amortised and never scans the log.
- **Backwards compatibility.** Existing tests call `tracker.spent(cardId)`. The solution keeps that
  method (lifetime total) and adds month-aware overloads, rather than breaking callers.

## What to say in the interview

- "Order of checks matters. The ticket says the overall limit wins, so it goes first. I'll make
  the order explicit in one method so it's easy to read."
- "In production, check-then-record must be atomic, or two simultaneous swipes could both pass.
  I'd use a DB transaction / row lock or `ConcurrentHashMap.compute`. I'm not building that here."
- "The velocity map grows per (card, merchant). I'd evict empty deques, or use a TTL cache."
- "Thresholds should be config: pass a `VelocityPolicy` record into the constructor."

## Common mistakes

- Recording spend before checking the category cap, so a declined attempt still uses up budget.
- Using `LocalDate.now()` or UTC for the month, which fails the 11:30 PM Toronto test.
- `>=` vs `>`: reaching a cap exactly must be allowed.
- Counting declined attempts in the velocity window.
