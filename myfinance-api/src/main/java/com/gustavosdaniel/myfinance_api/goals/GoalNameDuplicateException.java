package com.gustavosdaniel.myfinance_api.goals;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class GoalNameDuplicateException extends BaseExceptionRunTime {
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
