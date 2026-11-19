package com.acme.ledger;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountId, long balance, long requested) {
        super("Account " + accountId + " has " + balance + " cents, needs " + requested);
    }
}
