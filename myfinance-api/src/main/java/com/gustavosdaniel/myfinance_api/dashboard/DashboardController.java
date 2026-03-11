package com.gustavosdaniel.myfinance_api.dashboard;

import com.gustavosdaniel.myfinance_api.openApi.DashboardOpenApi;
import com.gustavosdaniel.myfinance_api.user.User;
import com.gustavosdaniel.myfinance_api.util.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController implements DashboardOpenApi {

    private final DashboardService dashboardService;
    private final AuthHelper authHelper;

    public DashboardController(DashboardService dashboardService, AuthHelper authHelper) {
        this.dashboardService = dashboardService;
        this.authHelper = authHelper;
    }

    @GetMapping
    @Operation(summary = "Mostra o Dashboard de gastos por categoria do usuário")
    public ResponseEntity<DashboardResponse> dashboard(
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @ModelAttribute BetweenDate date
            ){

        User user = authHelper.getCurrentUser(principal);

        DashboardResponse dashboard = dashboardService.getDashboard(user, date);

        return ResponseEntity.ok(dashboard);
    }
}
