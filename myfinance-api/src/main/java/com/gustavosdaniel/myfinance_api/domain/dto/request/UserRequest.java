package com.gustavosdaniel.myfinance_api.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(

        @NotBlank(message = "O ID do keycloak é obrigatório")
        String keycloakId,
        @NotBlank(message = "O email não pode ser vazio")
        @Email(message = "Formato de email incorreto")
        String email,
        @NotBlank(message = "O nome não pode ser vazio")
        String name) {
}
