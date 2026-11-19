package com.acme.review;

/**
 * Reviews card transactions against a company's spend policy.
 * Written in a hurry for launch. The mobile app parses the returned strings, so don't change them.
 */
public class TransactionReviewer {

    public String review(Transaction t, CompanySettings s) {
        String result = "APPROVED";
        if (t.amountCents() <= 0) {
            result = "DECLINED: invalid amount";
        } else {
            if (s.blockedMerchants() != null && s.blockedMerchants().contains(t.merchant().toLowerCase())) {
                result = "DECLINED: merchant blocked";
            } else if (t.amountCents() > s.maxSingleTransactionCents()) {
                result = "DECLINED: over single transaction limit";
            } else {
                if (!s.allowInternational() && !t.country().equals("CA")) {
                    result = "DECLINED: international not allowed";
                } else {
                    if (t.amountCents() > s.receiptRequiredOverCents()) {
                        result = "FLAGGED: receipt required";
                    }
                    if (t.mcc().equals("7995")) {   // gambling
                        result = "FLAGGED: gambling";
                    }
                }
            }
        }
        return result;
    }

    /** New structured API for the web dashboard. See TASK.md. */
    public ReviewDecision reviewAll(Transaction t, CompanySettings s) {
        throw new UnsupportedOperationException("TODO FIN-560");
    }
}
