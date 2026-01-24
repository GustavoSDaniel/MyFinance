package com.gustavosdaniel.myfinance_api.categories;

import com.gustavosdaniel.myfinance_api.exception.BaseExceptionRunTime;

public class CategoryNotFoundException extends BaseExceptionRunTime {

    public CategoryNotFoundException() {
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoryNotFoundException(Throwable cause) {
        super(cause);
    }

    public CategoryNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
