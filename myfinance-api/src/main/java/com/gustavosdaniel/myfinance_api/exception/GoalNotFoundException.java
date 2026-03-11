package com.gustavosdaniel.myfinance_api.exception;

public class GoalNotFoundException extends RuntimeException {

    public GoalNotFoundException() {
    }

    public GoalNotFoundException(String message) {
        super(message);
    }

    public GoalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoalNotFoundException(Throwable cause) {
        super(cause);
    }

    public GoalNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
