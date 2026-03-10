package com.gustavosdaniel.myfinance_api.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(

        @Email(message = "Formato de email incorreto")
        String email,
        @NotBlank(message = "O nome não pode ser vazio")
        String name,
        String picture) {
}
