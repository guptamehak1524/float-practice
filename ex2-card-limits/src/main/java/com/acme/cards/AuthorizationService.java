package com.acme.cards;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Decides in real time whether a card transaction is allowed.
 * The card network times out after about 2 seconds, so this must stay fast.
 */
public class AuthorizationService {

    private final CardRepository cards;
    private final SpendTracker spendTracker;
    private final Clock clock;
    private final List<AuthorizationRecord> log = new ArrayList<>();

    public AuthorizationService(CardRepository cards, SpendTracker spendTracker, Clock clock) {
        this.cards = cards;
        this.spendTracker = spendTracker;
        this.clock = clock;
    }

    public AuthorizationResult authorize(AuthorizationRequest request) {
        AuthorizationResult result = decide(request);
        log.add(new AuthorizationRecord(clock.instant(), request, result));
        return result;
    }

    /** Admins can cap spend per merchant category on a card, e.g. $500/month on MEALS. */
    public void setCategoryLimit(String cardId, MerchantCategory category, long limitCents) {
        throw new UnsupportedOperationException("TODO FIN-511: category limits");
    }

    /** Every authorization attempt for this card, oldest first. */
    public List<AuthorizationRecord> history(String cardId) {
        return log.stream().filter(r -> r.request().cardId().equals(cardId)).toList();
    }

    private AuthorizationResult decide(AuthorizationRequest request) {
        if (request.amountCents() <= 0) {
            return AuthorizationResult.decline(DeclineReason.INVALID_AMOUNT);
        }
        Optional<Card> maybeCard = cards.findById(request.cardId());
        if (maybeCard.isEmpty()) {
            return AuthorizationResult.decline(DeclineReason.CARD_NOT_FOUND);
        }
        Card card = maybeCard.get();
        if (card.status() != CardStatus.ACTIVE) {
            return AuthorizationResult.decline(DeclineReason.CARD_NOT_ACTIVE);
        }
        long spent = spendTracker.spent(card.id());
        if (spent + request.amountCents() > card.monthlyLimitCents()) {
            return AuthorizationResult.decline(DeclineReason.MONTHLY_LIMIT_EXCEEDED);
        }
        spendTracker.record(card.id(), request.amountCents());
        return AuthorizationResult.approve();
    }
}
