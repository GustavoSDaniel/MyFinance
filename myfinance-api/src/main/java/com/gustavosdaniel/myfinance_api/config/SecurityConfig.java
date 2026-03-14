package com.gustavosdaniel.myfinance_api.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );


        return http.build();

    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess == null || !realmAccess.containsKey("roles")){
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        });

        return converter;
    }
}
