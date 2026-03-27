package com.gustavosdaniel.myfinance_api.domain.dto.response;

public record ErroDocResponse(

        String title,
        String detail,
        String cause,
        String howToFix,
        int status
) {
}
