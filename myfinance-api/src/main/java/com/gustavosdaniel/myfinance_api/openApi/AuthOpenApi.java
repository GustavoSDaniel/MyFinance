package com.gustavosdaniel.myfinance_api.openApi;

import com.gustavosdaniel.myfinance_api.user.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Tag(
        name = "Autenticação",
        description = "Endpoints relacionados à autenticação e recuperação de dados do usuário autenticado"
)
public interface AuthOpenApi {

    @Operation(
            summary = "Obter dados do usuário autenticado",
            description = "Retorna as informações do usuário atualmente autenticado na aplicação."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados do usuário retornados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado ou token inválido"
            )
    })
    ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal OAuth2User principal);
}
