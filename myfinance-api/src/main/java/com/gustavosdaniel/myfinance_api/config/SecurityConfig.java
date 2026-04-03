package com.gustavosdaniel.myfinance_api.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuração de segurança da aplicação utilizando Spring Security com OAuth2 Resource Server (JWT).
 * <p>
 * Define as regras de autorização para os endpoints da API, configurando acesso público para
 * documentação Swagger, actuator e documentação de erros, além de exigir autenticação para
 * todos os demais endpoints. A autenticação é baseada em tokens JWT provenientes do Keycloak.
 * </p>
 * <p>
 * As roles são extraídas dos claims do token JWT, tanto do campo {@code realm_access} (roles do realm)
 * quanto do campo {@code resource_access} (roles específicas do cliente "my-finance-app").
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Lista de origens permitidas para as requisições CORS.
     * <p>
     * Os valores são carregados a partir da propriedade {@code app.cors.allowed-origins}
     * definida no {@code application.yml}, que por sua vez lê a variável de ambiente
     * {@code CORS_ALLOWED_ORIGINS}.
     * </p>
     * <p>
     * Múltiplas origens devem ser separadas por vírgula na variável de ambiente:
     * <pre>
     *   CORS_ALLOWED_ORIGINS=https://meu-frontend.com,https://admin.meu-frontend.com
     * </pre>
     * </p>
     */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * URLs públicas que não exigem autenticação.
     * <p>
     * Incluem documentação Swagger/OpenAPI, endpoints do Actuator e documentação de erros.
     * </p>
     */
    public static final String[] PUBLIC_URLS = {

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**",
            "/erros/**"
    };

    /**
     * Configura a cadeia de filtros de segurança.
     * <p>
     * Define:
     * <ul>
     *   <li>Desativação de CSRF (stateless)</li>
     *   <li>Gerenciamento de sessão STATELESS (sem estado)</li>
     *   <li>Regras de autorização para diferentes endpoints e métodos HTTP</li>
     *   <li>Configuração de Resource Server OAuth2 com JWT</li>
     * </ul>
     * </p>
     *
     * @param http o objeto {@link HttpSecurity} para configuração
     * @return a cadeia de filtros configurada
     * @throws Exception em caso de erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(PUBLIC_URLS).permitAll()

                        //user
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAnyRole("ADMIN", "USER")
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

    /**
     * Configura as permissões de CORS para integração com o frontend.
     * <p>
     * Define:
     * <ul>
     *   <li><b>Origins permitidas</b> – ajuste para a URL real do seu frontend</li>
     *   <li><b>Métodos permitidos</b> – GET, POST, PUT, PATCH, DELETE, OPTIONS</li>
     *   <li><b>Headers permitidos</b> – todos (necessário para envio do Authorization/JWT)</li>
     *   <li><b>Expose headers</b> – expõe o header Authorization para o frontend</li>
     *   <li><b>Allow credentials</b> – permite envio de cookies/credenciais</li>
     *   <li><b>Max age</b> – tempo de cache do preflight (OPTIONS) em segundos</li>
     * </ul>
     * </p>
     *
     * @return a fonte de configuração de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Substitua pela URL do seu frontend
        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Permite todos os headers, incluindo Authorization (JWT)
        configuration.setAllowedHeaders(List.of("*"));

        // Expõe o header Authorization para que o frontend possa lê-lo
        configuration.setExposedHeaders(List.of("Authorization"));

        // Necessário para envio de cookies ou tokens via header
        configuration.setAllowCredentials(true);

        // Tempo (em segundos) que o browser pode cachear a resposta do preflight (OPTIONS)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Configura o conversor de autenticação JWT para extrair as roles dos claims do token.
     * <p>
     * As roles são extraídas de duas fontes:
     * <ul>
     *   <li><b>realm_access.roles</b> – roles globais do realm no Keycloak</li>
     *   <li><b>resource_access.my-finance-app.roles</b> – roles específicas do cliente "my-finance-app"</li>
     * </ul>
     * Cada role é prefixada com {@code ROLE_} para compatibilidade com o Spring Security.
     * </p>
     *
     * @return o conversor configurado
     */
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
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                    .toList()
                    );
                }
            }

            return authorities;
        });

        return converter;
    }
}
