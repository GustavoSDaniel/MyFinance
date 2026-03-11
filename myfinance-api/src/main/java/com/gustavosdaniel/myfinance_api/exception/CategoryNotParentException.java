package com.gustavosdaniel.myfinance_api.exception;

public class CategoryNotParentException extends RuntimeException {

    public CategoryNotParentException() {
    }

    public CategoryNotParentException(String message) {
        super(message);
    }

    public CategoryNotParentException(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoryNotParentException(Throwable cause) {
        super(cause);
    }

    public CategoryNotParentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
