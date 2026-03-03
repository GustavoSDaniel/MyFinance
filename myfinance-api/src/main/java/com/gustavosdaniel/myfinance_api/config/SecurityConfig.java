package com.gustavosdaniel.myfinance_api.config;

import com.gustavosdaniel.myfinance_api.util.CustomOAuth2UserService;
import com.gustavosdaniel.myfinance_api.util.OAuth2LoginSuccessHandler;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    public static final String[] PUBLIC_URLS = {

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**"


    };

    public SecurityConfig(OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler, CustomOAuth2UserService customOAuth2UserService) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll()

                        //user
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/allUsers/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/email/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/*").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/*")
                                .hasAnyRole("ADMIN", "USER")

                        //account
                                .requestMatchers(HttpMethod.POST, "/api/v1/accounts/**").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/**").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/accounts/**").hasAnyRole("ADMIN", "USER")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/accounts/**").hasAnyRole("ADMIN", "USER")

                        //category
                                .requestMatchers("/api/v1/categories/**").hasAnyRole("ADMIN", "USER")

                        // Transaction
                                .requestMatchers("/api/v1/transactions/**").hasAnyRole("ADMIN", "USER")

                        // Goal
                                .requestMatchers("/api/v1/goals/**").hasAnyRole("ADMIN", "USER")

                        // Dashboard
                                .requestMatchers("/api/v1/dashboards").hasAnyRole("ADMIN", "USER")


                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl("/login?error=true")
                )

                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );


        return http.build();

    }
}
