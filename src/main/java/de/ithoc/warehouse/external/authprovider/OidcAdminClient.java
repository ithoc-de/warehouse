package de.ithoc.warehouse.external.authprovider;

import de.ithoc.warehouse.domain.synchronization.MultipleOAuth2UsersException;
import de.ithoc.warehouse.external.schema.keycloak.token.Token;
import de.ithoc.warehouse.external.schema.keycloak.users.User;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.UserModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class OidcAdminClient {

    private final WebClient webClient;
    private final URI adminApiUrl;

    public OidcAdminClient(
            WebClient webClient,
            @Value("${oidc.admin-api.baseUrl}") String adminApiUrl
    ) {
        this.webClient = webClient;
        this.adminApiUrl = URI.create(adminApiUrl);
    }


    public List<User> getUsers(Token token) {

        ParameterizedTypeReference<List<User>> parameterizedTypeReference = new ParameterizedTypeReference<>() {};
        return webClient.get()
                .uri(adminApiUrl + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .retrieve()
                .bodyToMono(parameterizedTypeReference)
                .block();
    }


    public Optional<User> getUserBy(String email, Token token) {

        // http://localhost:7002/admin/realms/Solution4Europe/users?email=oliver.hock@gmail.com&exact=true
        ParameterizedTypeReference<List<User>> parameterizedTypeReference = new ParameterizedTypeReference<>() {};
        List<User> users = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(adminApiUrl.getScheme())
                        .host(adminApiUrl.getHost())
                        .port(adminApiUrl.getPort())
                        .path(adminApiUrl.getPath() + "/users")
                        .queryParam("email", email)
                        .queryParam("exact", "true")
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .retrieve()
                .bodyToMono(parameterizedTypeReference)
                .block();

        User user = null;
        if(users != null) {
            if(users.size() == 1) {
                user = users.get(0);
            } else if(users.size() > 1) {
                String message = "OAuth2 error on provider: " +
                        "Multiple users exist for given e-mail address '" + email + "'";
                log.error(message);
                throw new MultipleOAuth2UsersException("Ambiguous users exist: " + users.size());
            }
        }
        log.debug("user: {}", user);

        return Optional.ofNullable(user);
    }


    /**
     *     enum RequiredAction {
     *         VERIFY_EMAIL,
     *         UPDATE_PROFILE,
     *         CONFIGURE_TOTP,
     *         CONFIGURE_RECOVERY_AUTHN_CODES,
     *         UPDATE_PASSWORD,
     *         TERMS_AND_CONDITIONS,
     *         VERIFY_PROFILE,
     *         UPDATE_EMAIL
     *     }
     *
     * @param requiredAction Required action as Keycloak enum (Maven dependency 'keycloak-server-spi').
     * @return requiredAction as String
     */
    public String requiredActionAsString(UserModel.RequiredAction requiredAction) {

        return requiredAction.name();
    }


    public User postUser(User user, Token token) {

        User block = webClient.post()
                .uri(adminApiUrl + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
                .body(BodyInserters.fromValue(user))
                .retrieve()
                .bodyToMono(User.class)
                .block();
        log.debug("bock: {}", block);

        return block;
    }

}
