package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class AccountNotFoundException extends BaseExceptionRunTime {

    public AccountNotFoundException() {
    }

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountNotFoundException(Throwable cause) {
        super(cause);
    }

    public AccountNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
