package com.gustavosdaniel.myfinance_api.erro;

public record ErrorDoc(

        String title,
        String detail,
        String cause,
        String howToFix,
        int status
) {
}
