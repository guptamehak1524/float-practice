package com.acme.review;

import java.util.List;

/** Structured review result: the overall outcome and every reason that applied. */
public record ReviewDecision(Outcome outcome, List<String> reasons) {
}
