package com.gustavosdaniel.myfinance_api.openApi;

import com.gustavosdaniel.myfinance_api.dashboard.BetweenDate;
import com.gustavosdaniel.myfinance_api.dashboard.DashboardResponse;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ModelAttribute;

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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "Dashboard", description = "API responsável por fornecer dados consolidados e métricas para o dashboard financeiro do usuário")
public interface DashboardOpenApi {

    @Operation(
            summary = "Obter dados do dashboard",
            description = "Retorna informações financeiras consolidadas (receitas, despesas, saldo total, gastos por categoria) do usuário autenticado. Os dados devem ser filtrados por um período específico através de datas de início e fim."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do dashboard retornados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de data inválidos (ex: formato incorreto, ou data inicial maior que a final)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    ResponseEntity<DashboardResponse> dashboard(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(
                    description = "Objeto contendo o filtro de período para o dashboard (data inicial e data final)",
                    required = true
            )
            @Valid @ModelAttribute BetweenDate date
    );
}