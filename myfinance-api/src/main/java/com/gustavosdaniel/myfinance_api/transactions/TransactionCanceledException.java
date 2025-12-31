package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class TransactionCanceledException extends BaseExceptionRunTime {

    public TransactionCanceledException() {
    }

    public TransactionCanceledException(String message) {
        super(message);
    }

    public TransactionCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionCanceledException(Throwable cause) {
        super(cause);
    }

    public TransactionCanceledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
