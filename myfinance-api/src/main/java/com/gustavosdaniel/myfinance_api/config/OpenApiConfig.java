package com.gustavosdaniel.myfinance_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação OpenAPI (Swagger) para a API.
 * <p>
 * Define as informações gerais da API (título, versão, descrição) e configura
 * o esquema de autenticação Bearer JWT, utilizado para proteger os endpoints
 * que exigem autenticação.
 * </p>
 * <p>
 * A documentação gerada estará disponível em:
 * <ul>
 *   <li>{@code /v3/api-docs} – especificação OpenAPI em formato JSON</li>
 *   <li>{@code /swagger-ui.html} – interface interativa Swagger UI</li>
 * </ul>
 * </p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Cria e configura o bean {@link OpenAPI} com as informações da API
     * e o esquema de segurança Bearer JWT.
     *
     * @return objeto {@link OpenAPI} configurado para documentação
     */
    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("My Finance API")
                        .version("1.0")
                        .description("API para gerenciamento de finanças pessoais"))

                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT gerado pelo Keycloak " +
                                        "(sem a palavra 'Bearer')")
                        )
                );

    }
}
