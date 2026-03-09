package com.gustavosdaniel.myfinance_api.domain.dto;

import com.gustavosdaniel.myfinance_api.domain.enuns.CategoryType;

import java.util.UUID;

public record CategoryResponse(

        UUID id,
        String name,
        CategoryType type,
        String color

) {
}
