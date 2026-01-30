package com.gustavosdaniel.myfinance_api.categories;

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
