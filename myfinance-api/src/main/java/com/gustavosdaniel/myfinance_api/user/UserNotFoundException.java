package com.gustavosdaniel.myfinance_api.user;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class UserNotFoundException extends BaseExceptionRunTime {

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    public UserNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
