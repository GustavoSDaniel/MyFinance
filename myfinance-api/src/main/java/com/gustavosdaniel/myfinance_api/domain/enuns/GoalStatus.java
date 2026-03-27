package com.gustavosdaniel.myfinance_api.domain.enuns;

public enum GoalStatus {

    ACHIEVED,
    PROGRESS,
    ALL;

    public static GoalStatus fromString(String status){

        if (status == null) return ALL;

        return switch (status){

            case "achieved" -> ACHIEVED;

            case "progress" -> PROGRESS;

            default -> ALL;
        };
    }
}
