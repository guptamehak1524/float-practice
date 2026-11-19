package com.acme.cards;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks how much each card has spent against its limit.
 * NOTE: spend is never reset. The monthly reset job was planned but never built.
 */
public class SpendTracker {

    private final Map<String, Long> spentByCard = new HashMap<>();

    public long spent(String cardId) {
        return spentByCard.getOrDefault(cardId, 0L);
    }

    public void record(String cardId, long amountCents) {
        spentByCard.merge(cardId, amountCents, Long::sum);
    }
}
