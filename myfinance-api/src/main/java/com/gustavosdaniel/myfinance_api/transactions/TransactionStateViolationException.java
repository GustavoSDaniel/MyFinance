package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class TransactionStateViolationException extends BaseExceptionRunTime {

    public TransactionStateViolationException() {
    }

    public TransactionStateViolationException(String message) {
        super(message);
    }

    public TransactionStateViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionStateViolationException(Throwable cause) {
        super(cause);
    }

    public TransactionStateViolationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
