package com.gustavosdaniel.myfinance_api.dashboard;

import com.gustavosdaniel.myfinance_api.user.User;

public interface DashboardService {

    DashboardResponse getDashboard(User user,BetweenDate betweenDate);
}
