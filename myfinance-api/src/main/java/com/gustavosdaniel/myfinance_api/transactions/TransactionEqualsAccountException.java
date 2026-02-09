package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class TransactionEqualsAccountException extends BaseExceptionRunTime {

    public TransactionEqualsAccountException() {
    }

    public TransactionEqualsAccountException(String message) {
        super(message);
    }

    public TransactionEqualsAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionEqualsAccountException(Throwable cause) {
        super(cause);
    }

    public TransactionEqualsAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
