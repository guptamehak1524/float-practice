package com.acme.review;

import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

/** Spend policy a company admin configures. Build with {@link #builder(String)}. */
public class CompanySettings {

    private final String companyId;
    private final long maxSingleTransactionCents;
    private final Set<String> blockedMerchants;     // stored lower-case
    private final Set<String> blockedMccs;
    private final boolean allowInternational;
    private final long receiptRequiredOverCents;
    private final boolean blockWeekends;
    private final ZoneId zone;

    private CompanySettings(Builder b) {
        this.companyId = b.companyId;
        this.maxSingleTransactionCents = b.maxSingleTransactionCents;
        // Normalise once, when settings are built, so every rule can trust the data.
        this.blockedMerchants = b.blockedMerchants.stream().map(String::toLowerCase).collect(Collectors.toUnmodifiableSet());
        this.blockedMccs = Set.copyOf(b.blockedMccs);
        this.allowInternational = b.allowInternational;
        this.receiptRequiredOverCents = b.receiptRequiredOverCents;
        this.blockWeekends = b.blockWeekends;
        this.zone = b.zone;
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
    public boolean blockWeekends() { return blockWeekends; }
    public ZoneId zone() { return zone; }

    public static class Builder {
        private final String companyId;
        private long maxSingleTransactionCents = Long.MAX_VALUE;
        private Set<String> blockedMerchants = Set.of();
        private Set<String> blockedMccs = Set.of();
        private boolean allowInternational = true;
        private long receiptRequiredOverCents = 75_00;
        private boolean blockWeekends = false;
        private ZoneId zone = ZoneId.of("America/Toronto");

        private Builder(String companyId) { this.companyId = companyId; }

        public Builder maxSingleTransactionCents(long v) { this.maxSingleTransactionCents = v; return this; }
        public Builder blockedMerchants(Set<String> v) { this.blockedMerchants = v; return this; }
        public Builder blockedMccs(Set<String> v) { this.blockedMccs = v; return this; }
        public Builder allowInternational(boolean v) { this.allowInternational = v; return this; }
        public Builder receiptRequiredOverCents(long v) { this.receiptRequiredOverCents = v; return this; }
        public Builder blockWeekends(boolean v) { this.blockWeekends = v; return this; }
        public Builder zone(ZoneId v) { this.zone = v; return this; }
        public CompanySettings build() { return new CompanySettings(this); }
    }
}
