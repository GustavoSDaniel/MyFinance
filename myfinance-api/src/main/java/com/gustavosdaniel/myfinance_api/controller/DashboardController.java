package com.gustavosdaniel.myfinance_api.controller;

import com.gustavosdaniel.myfinance_api.domain.dto.BetweenDateDashboard;
import com.gustavosdaniel.myfinance_api.domain.dto.DashboardResponse;
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

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> dashboard(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute BetweenDateDashboard date
            ){

        return dashboardService.getDashboard(jwt, date);
    }
}
