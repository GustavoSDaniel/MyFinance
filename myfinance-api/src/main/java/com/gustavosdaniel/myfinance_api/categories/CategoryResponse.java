package com.gustavosdaniel.myfinance_api.categories;

import java.util.UUID;

public record CategoryResponse(

        UUID id,
        String name,
        CategoryType type,
        String color

) {
}
