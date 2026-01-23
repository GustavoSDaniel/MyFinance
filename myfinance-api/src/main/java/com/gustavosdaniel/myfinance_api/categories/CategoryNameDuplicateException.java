package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.exception.BusinesException;

public class CategoryNameDuplicateException extends BusinesException {

    public CategoryNameDuplicateException() {
    }

    public CategoryNameDuplicateException(String message) {
        super(message);
    }

    public CategoryNameDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoryNameDuplicateException(Throwable cause) {
        super(cause);
    }

    public CategoryNameDuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
