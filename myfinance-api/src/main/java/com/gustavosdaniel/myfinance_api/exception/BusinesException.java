package com.gustavosdaniel.myfinance_api.exception;

public class BusinesException extends Exception{

    public BusinesException() {
    }

    public BusinesException(String message) {
        super(message);
    }

    public BusinesException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinesException(Throwable cause) {
        super(cause);
    }

    public BusinesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
