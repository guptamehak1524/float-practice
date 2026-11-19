package com.acme.cards;

/** Outcome of an authorization. {@code reason} is null when approved. */
public record AuthorizationResult(boolean approved, DeclineReason reason) {

    public static AuthorizationResult approve() {
        return new AuthorizationResult(true, null);
    }

    public static AuthorizationResult decline(DeclineReason reason) {
        return new AuthorizationResult(false, reason);
    }
}
