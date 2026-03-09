package com.gustavosdaniel.myfinance_api.exception;

public class AccountAlreadyActiveException extends RuntimeException{

    public AccountAlreadyActiveException() {
    }

    public AccountAlreadyActiveException(String message) {
        super(message);
    }

    public AccountAlreadyActiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountAlreadyActiveException(Throwable cause) {
        super(cause);
    }

    public AccountAlreadyActiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
