package com.gustavosdaniel.myfinance_api.exception;

public class AccountAlreadyDeactivateException extends RuntimeException{

    public AccountAlreadyDeactivateException() {
    }

    public AccountAlreadyDeactivateException(String message) {
        super(message);
    }

    public AccountAlreadyDeactivateException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountAlreadyDeactivateException(Throwable cause) {
        super(cause);
    }

    public AccountAlreadyDeactivateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
