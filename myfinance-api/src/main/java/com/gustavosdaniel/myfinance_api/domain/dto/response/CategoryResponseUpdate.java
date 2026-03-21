package com.gustavosdaniel.myfinance_api.domain.dto.response;

import com.gustavosdaniel.myfinance_api.domain.enuns.CategoryType;

import java.util.UUID;

public record CategoryResponseUpdate(

        UUID id,
        String name,
        CategoryType type,
        String color,
        String description,
        String icon

) {
}
