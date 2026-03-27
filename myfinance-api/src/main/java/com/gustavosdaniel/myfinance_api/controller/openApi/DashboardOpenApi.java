package com.gustavosdaniel.myfinance_api.controller.openApi;

import com.gustavosdaniel.myfinance_api.domain.dto.request.BetweenDateDashboard;
import com.gustavosdaniel.myfinance_api.domain.dto.response.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(
        name = "Dashboard",
        description = "API responsável por fornecer dados consolidados do dashboard financeiro do usuário"
)
public interface DashboardOpenApi {

    @Operation(
            summary = "Obter dados do dashboard",
            description = "Retorna informações consolidadas do dashboard financeiro do usuário autenticado, podendo ser filtradas por período."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados do dashboard retornados com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = DashboardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de data inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            )
    })
    ResponseEntity<DashboardResponse> dashboard(

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,

            @Parameter(
                    description = "Filtro de período para o dashboard (data inicial e final)",
                    required = false
            )
            @Valid @ModelAttribute BetweenDateDashboard date
    );
}
