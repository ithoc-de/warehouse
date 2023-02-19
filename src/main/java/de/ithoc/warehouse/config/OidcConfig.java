package de.ithoc.warehouse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
public class OidcConfig {

    private final KeycloakLogoutHandler keycloakLogoutHandler;

    public OidcConfig(KeycloakLogoutHandler keycloakLogoutHandler) {
        this.keycloakLogoutHandler = keycloakLogoutHandler;
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/warehouse/*").hasRole("USER")
                .antMatchers("/userinfo").hasRole("ADMIN")
                .anyRequest().authenticated();

        httpSecurity.oauth2Login()
                .and()
                .logout()
                .addLogoutHandler(keycloakLogoutHandler)
                .logoutSuccessUrl("/");

        httpSecurity.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

        return httpSecurity.build();
    }

}
