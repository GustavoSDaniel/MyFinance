package com.gustavosdaniel.myfinance_api.exception;

public class TransactionEqualsAccountException extends RuntimeException {

    public TransactionEqualsAccountException() {
    }

    public TransactionEqualsAccountException(String message) {
        super(message);
    }

    public TransactionEqualsAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionEqualsAccountException(Throwable cause) {
        super(cause);
    }

    public TransactionEqualsAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
