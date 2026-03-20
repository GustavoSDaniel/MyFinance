package com.gustavosdaniel.myfinance_api.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String[] PUBLIC_URLS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**",
            "/erros/**"
    };

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
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/me")
                                .hasAnyRole("ADMIN", "USER")
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
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();

    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess != null && realmAccess.containsKey("roles")) {

                List<String> roles = (List<String>) realmAccess.get("roles");

                authorities.addAll(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority(
                                        "ROLE_" + role.toUpperCase()))
                                .toList()
                );
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess != null) {

                Map<String, Object> client = (Map<String, Object>) resourceAccess
                        .get("my-finance-app");

                if (client != null && client.containsKey("roles")) {

                    List<String> roles = (List<String>) client.get("roles");

                    authorities.addAll(
                            roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" +
                                            role.toUpperCase()))
                                    .toList()
                    );
                }
            }

            return authorities;
        });

        return converter;
    }
}
