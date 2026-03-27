package com.gustavosdaniel.myfinance_api.domain.enuns;

public enum Status {

    ACTIVE,
    DISABLED,
    ALL;


    public static Status fromString(String status) {

        if (status == null) return ALL;

        return switch (status.toLowerCase()){

            case "active" -> ACTIVE;
            case "disabled" -> DISABLED;
            default -> ALL;
        };
    }

}
