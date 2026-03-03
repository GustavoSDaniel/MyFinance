package com.gustavosdaniel.myfinance_api.user;

import java.util.UUID;

public record UserResponse(

        UUID id,
        String name,
        String email
) {
}
