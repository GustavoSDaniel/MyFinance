package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.exception.BusinesException;

public class InvalidAmountException extends BusinesException {

    public InvalidAmountException() {
    }

    public InvalidAmountException(String message) {
        super(message);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAmountException(Throwable cause) {
        super(cause);
    }

    public InvalidAmountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
