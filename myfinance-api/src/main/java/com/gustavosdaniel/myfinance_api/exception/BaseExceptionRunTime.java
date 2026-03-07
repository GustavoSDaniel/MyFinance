package com.gustavosdaniel.myfinance_api.exception;

public class BaseExceptionRunTime extends RuntimeException {

    public BaseExceptionRunTime() {
    }

    public BaseExceptionRunTime(String message) {
        super(message);
    }

    public BaseExceptionRunTime(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseExceptionRunTime(Throwable cause) {
        super(cause);
    }

    public BaseExceptionRunTime(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
