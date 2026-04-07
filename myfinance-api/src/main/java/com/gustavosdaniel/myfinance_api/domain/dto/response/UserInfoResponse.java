package com.gustavosdaniel.myfinance_api.domain.dto.response;

import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;

import java.util.UUID;

public record UserInfoResponse(

        UUID id,
        String name,
        String email,
        UserRole role,
        String picture
) {
}
