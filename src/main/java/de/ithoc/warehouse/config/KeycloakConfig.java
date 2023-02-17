package de.ithoc.warehouse.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration resolves cycle dependencies:
 *
 * keycloakSecurityConfiguration (field private org.keycloak.adapters.KeycloakConfigResolver
 * org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter.keycloakConfigResolver)
 *
 * <a href="https://stackoverflow.com/questions/70207564/spring-boot-2-6-regression-how-can-i-fix-keycloak-circular-dependency-in-adapte">
 *     Spring Boot 2.6 regression: How can I fix Keycloak circular dependency in adapter?
 * </a>
 */
@Configuration
public class KeycloakConfig {

    @Bean
    public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

}
