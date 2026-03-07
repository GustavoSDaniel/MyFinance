package com.gustavosdaniel.myfinance_api.exception;

public class IdempotencyKeyException extends BaseExceptionRunTime {

    public IdempotencyKeyException() {
    }

    public IdempotencyKeyException(String message) {
        super(message);
    }

    public IdempotencyKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdempotencyKeyException(Throwable cause) {
        super(cause);
    }

    public IdempotencyKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
