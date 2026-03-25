package com.gustavosdaniel.myfinance_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuração JPA para habilitar o recurso de auditoria.
 * <p>
 * A anotação {@link EnableJpaAuditing} ativa o suporte a auditoria,
 * permitindo que entidades anotadas com {@link org.springframework.data.annotation.CreatedDate},
 * {@link org.springframework.data.annotation.LastModifiedDate},
 * {@link org.springframework.data.annotation.CreatedBy} e
 * {@link org.springframework.data.annotation.LastModifiedBy} tenham seus campos preenchidos
 * automaticamente pelo Spring Data JPA.
 * </p>
 * <p>
 * Esta classe pode ser estendida no futuro para configurar provedores de auditoria,
 * como a definição do auditor atual através de um bean {@link org.springframework.data.domain.AuditorAware}.
 * </p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
