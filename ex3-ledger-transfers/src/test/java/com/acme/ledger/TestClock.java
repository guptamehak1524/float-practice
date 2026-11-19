package com.acme.ledger;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock tests can move forward by hand. */
class TestClock extends Clock {
    private Instant now;

    TestClock(Instant start) { this.now = start; }

    void advance(Duration d) { now = now.plus(d); }

    @Override public Instant instant() { return now; }
    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
    @Override public Clock withZone(ZoneId zone) { return this; }
}
