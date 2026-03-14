package com.gustavosdaniel.myfinance_api.exception;

public class AccountNameDuplicateException extends RuntimeException {

    public AccountNameDuplicateException() {
    }

    public AccountNameDuplicateException(String message) {
        super(message);
    }

    public AccountNameDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountNameDuplicateException(Throwable cause) {
        super(cause);
    }

    public AccountNameDuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
