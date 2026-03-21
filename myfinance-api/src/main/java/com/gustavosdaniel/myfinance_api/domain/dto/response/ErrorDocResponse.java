package com.gustavosdaniel.myfinance_api.domain.dto.response;

public record ErrorDocResponse(

        String title,
        String detail,
        String cause,
        String howToFix,
        int status
) {
}
