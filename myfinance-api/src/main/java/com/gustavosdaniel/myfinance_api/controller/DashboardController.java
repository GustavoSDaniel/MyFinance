package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.controller.metrics.DashboardMetrics;
import com.gustavosdaniel.myfinance_api.domain.dto.request.BetweenDateDashboard;
import com.gustavosdaniel.myfinance_api.domain.dto.response.DashboardResponse;
import com.gustavosdaniel.myfinance_api.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardMetrics dashboardMetrics;

    public DashboardController(DashboardService dashboardService, DashboardMetrics dashboardMetrics) {
        this.dashboardService = dashboardService;
        this.dashboardMetrics = dashboardMetrics;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> dashboard(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute BetweenDateDashboard date
            ){

        return dashboardMetrics.dashboard(() -> dashboardService.getDashboard(jwt, date));
    }
}
