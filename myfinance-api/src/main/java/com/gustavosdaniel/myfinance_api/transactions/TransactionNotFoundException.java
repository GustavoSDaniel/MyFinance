package com.gustavosdaniel.myfinance_api.transactions;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class TransactionNotFoundException extends BaseExceptionRunTime {

    public TransactionNotFoundException() {
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionNotFoundException(Throwable cause) {
        super(cause);
    }

    public TransactionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
