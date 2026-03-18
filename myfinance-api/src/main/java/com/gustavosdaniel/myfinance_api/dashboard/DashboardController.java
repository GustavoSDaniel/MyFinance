package com.gustavosdaniel.myfinance_api.dashboard;

import com.gustavosdaniel.myfinance_api.openApi.DashboardOpenApi;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST responsável por fornecer os dados para o dashboard financeiro.
 *
 * <p>Disponibiliza endpoints para a visualização consolidada de gastos, receitas
 * e saldos do usuário autenticado, baseados em um determinado período de tempo.
 */
@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController implements DashboardOpenApi {

    private final DashboardService dashboardService;
    private final AuthHelper authHelper;

    public DashboardController(DashboardService dashboardService, AuthHelper authHelper) {
        this.dashboardService = dashboardService;
        this.authHelper = authHelper;
    }

    /**
     * Retorna os dados consolidados do dashboard financeiro do usuário autenticado.
     *
     * @param jwt  o token JWT contendo as credenciais do usuário
     * @param date objeto contendo o intervalo de datas (início e fim) para filtro dos dados do dashboard
     * @return um {@link ResponseEntity} contendo o {@link DashboardResponse} com os dados consolidados
     */
    @GetMapping
    @Operation(summary = "Mostra o Dashboard de gastos por categoria do usuário")
    public ResponseEntity<DashboardResponse> dashboard(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute BetweenDate date
            ){

        User user = authHelper.getCurrentUser(jwt);

        DashboardResponse dashboard = dashboardService.getDashboard(user, date);

        return ResponseEntity.ok(dashboard);
    }
}
