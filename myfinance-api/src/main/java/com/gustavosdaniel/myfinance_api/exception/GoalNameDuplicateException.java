package com.gustavosdaniel.myfinance_api.exception;

public class GoalNameDuplicateException extends RuntimeException {
    public GoalNameDuplicateException() {
    }

    public GoalNameDuplicateException(String message) {
        super(message);
    }

    public GoalNameDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoalNameDuplicateException(Throwable cause) {
        super(cause);
    }

    public GoalNameDuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
