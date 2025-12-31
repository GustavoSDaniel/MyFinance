package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.exception.BusinesException;

public class CategoryNotParentException extends BusinesException {

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
