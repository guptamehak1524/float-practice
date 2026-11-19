package com.acme.review;

import java.time.Instant;

/**
 * A card transaction to review.
 *
 * @param mcc     4-digit merchant category code from the card network, e.g. "5812" restaurants, "7995" gambling
 * @param country ISO country code of the merchant, e.g. "CA", "US"
 */
public record Transaction(String id, String cardId, String merchant, String mcc, long amountCents, Instant at, String country) {
}
