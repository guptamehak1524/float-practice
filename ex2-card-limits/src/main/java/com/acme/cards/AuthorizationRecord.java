package com.acme.cards;

import java.time.Instant;

/** One line in the authorization log, kept for support and disputes. */
public record AuthorizationRecord(Instant at, AuthorizationRequest request, AuthorizationResult result) {
}
