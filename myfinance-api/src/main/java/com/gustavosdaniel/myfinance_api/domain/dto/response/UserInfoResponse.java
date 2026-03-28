package com.gustavosdaniel.myfinance_api.domain.dto.response;

import java.util.UUID;

public record UserInfoResponse(

        UUID id,
        String name,
        String email,
        String picture
) {
}
