package com.acme.review;

import java.util.Set;

/** Spend policy a company admin configures. Build with {@link #builder(String)}. */
public class CompanySettings {

    private final String companyId;
    private final long maxSingleTransactionCents;
    private final Set<String> blockedMerchants;
    private final Set<String> blockedMccs;          // added by the policy team last sprint; not enforced yet
    private final boolean allowInternational;
    private final long receiptRequiredOverCents;

    private CompanySettings(Builder b) {
        this.companyId = b.companyId;
        this.maxSingleTransactionCents = b.maxSingleTransactionCents;
        this.blockedMerchants = Set.copyOf(b.blockedMerchants);
        this.blockedMccs = Set.copyOf(b.blockedMccs);
        this.allowInternational = b.allowInternational;
        this.receiptRequiredOverCents = b.receiptRequiredOverCents;
    }

    public static Builder builder(String companyId) {
        return new Builder(companyId);
    }

    public String companyId() { return companyId; }
    public long maxSingleTransactionCents() { return maxSingleTransactionCents; }
    public Set<String> blockedMerchants() { return blockedMerchants; }
    public Set<String> blockedMccs() { return blockedMccs; }
    public boolean allowInternational() { return allowInternational; }
    public long receiptRequiredOverCents() { return receiptRequiredOverCents; }

    public static class Builder {
        private final String companyId;
        private long maxSingleTransactionCents = Long.MAX_VALUE;
        private Set<String> blockedMerchants = Set.of();
        private Set<String> blockedMccs = Set.of();
        private boolean allowInternational = true;
        private long receiptRequiredOverCents = 75_00;

        private Builder(String companyId) { this.companyId = companyId; }

        public Builder maxSingleTransactionCents(long v) { this.maxSingleTransactionCents = v; return this; }
        public Builder blockedMerchants(Set<String> v) { this.blockedMerchants = v; return this; }
        public Builder blockedMccs(Set<String> v) { this.blockedMccs = v; return this; }
        public Builder allowInternational(boolean v) { this.allowInternational = v; return this; }
        public Builder receiptRequiredOverCents(long v) { this.receiptRequiredOverCents = v; return this; }
        public CompanySettings build() { return new CompanySettings(this); }
    }
}
