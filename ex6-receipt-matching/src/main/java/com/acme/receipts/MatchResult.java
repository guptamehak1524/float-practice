package com.acme.receipts;

import java.util.List;

/**
 * @param matches                 in the order the receipts were given
 * @param unmatchedTransactionIds in the order the transactions were given
 * @param unmatchedReceiptIds     in the order the receipts were given
 */
public record MatchResult(List<Match> matches, List<String> unmatchedTransactionIds, List<String> unmatchedReceiptIds) {
}
