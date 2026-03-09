package com.gustavosdaniel.myfinance_api.exception;

public class TransactionStateViolationException extends RuntimeException {

    public TransactionStateViolationException() {
    }

    public TransactionStateViolationException(String message) {
        super(message);
    }

    public TransactionStateViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionStateViolationException(Throwable cause) {
        super(cause);
    }

    public TransactionStateViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
