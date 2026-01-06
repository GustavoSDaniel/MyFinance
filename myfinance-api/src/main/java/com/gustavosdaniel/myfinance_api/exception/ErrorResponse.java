package com.gustavosdaniel.myfinance_api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(

        String errorName,
        String message,
        LocalDateTime timesTamp,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> fieldsErrors
) {
}
