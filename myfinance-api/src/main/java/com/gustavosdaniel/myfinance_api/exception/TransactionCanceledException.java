package com.gustavosdaniel.myfinance_api.exception;

public class TransactionCanceledException extends RuntimeException {

    public TransactionCanceledException() {
    }

    public TransactionCanceledException(String message) {
        super(message);
    }

    public TransactionCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionCanceledException(Throwable cause) {
        super(cause);
    }

    public TransactionCanceledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
