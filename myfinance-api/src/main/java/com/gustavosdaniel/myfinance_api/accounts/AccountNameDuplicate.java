package com.gustavosdaniel.myfinance_api.accounts;

import com.gustavosdaniel.myfinance_api.exception.BusinesException;

public class AccountNameDuplicate extends BusinesException {

    public AccountNameDuplicate() {
    }

    public AccountNameDuplicate(String message) {
        super(message);
    }

    public AccountNameDuplicate(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountNameDuplicate(Throwable cause) {
        super(cause);
    }

    public AccountNameDuplicate(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
