package com.gustavosdaniel.myfinance_api.domain.dto;

import java.util.UUID;

public record UserResponse(

        UUID id,
        String name,
        String email
) {
}
