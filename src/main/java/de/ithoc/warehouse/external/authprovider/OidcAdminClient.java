package de.ithoc.warehouse.external.authprovider;

import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
public class OidcAdminClient {

    private final WebClient webClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String adminApiUrl;

    public OidcAdminClient(
            WebClient webClient,
            @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}") String tokenUri,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String clientSecret,
            @Value("${oidc.admin-api.username}") String username,
            @Value("${oidc.admin-api.password}") String password,
            @Value("${oidc.admin-api.baseUrl}") String adminApiUrl
    ) {
        this.webClient = webClient;
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
        this.adminApiUrl = adminApiUrl;
    }


    public Token token() {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);

        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Token.class)
                .publishOn(Schedulers.boundedElastic())
                .block();
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

}
