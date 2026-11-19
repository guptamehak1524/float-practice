package com.acme.cards;

/** A card network asking "can this card spend this amount at this merchant?". */
public record AuthorizationRequest(String cardId, String merchant, MerchantCategory category, long amountCents) {
}
