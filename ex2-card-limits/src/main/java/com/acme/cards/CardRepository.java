package com.acme.cards;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CardRepository {

    private final Map<String, Card> cards = new HashMap<>();

    public void save(Card card) {
        cards.put(card.id(), card);
    }

    public Optional<Card> findById(String id) {
        return Optional.ofNullable(cards.get(id));
    }
}
