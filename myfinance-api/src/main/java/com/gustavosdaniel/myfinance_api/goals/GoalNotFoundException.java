package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class GoalNotFoundException extends BaseExceptionRunTime {

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
