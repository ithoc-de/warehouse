package de.ithoc.warehouse.external.authprovider;

import de.ithoc.warehouse.external.schema.keycloak.token.Token;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class OidcTokenClientTest {

    private final MockWebServer mockWebServer = new MockWebServer();

    private static final String ACCESS_TOKEN
            = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiBiReSUIiwia2lkIiA6ICJBM2ZfYXdGSHNJdlFKVG9TcS04R0Y3SEJ";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    void token() throws IOException {

        String baseUrl = "http://localhost:" + mockWebServer.getPort();
        String tokenPath = "/realms/Solution4Europe/protocol/openid-connect/token";

        OidcTokenClient oidcAdminClient = new OidcTokenClient(
                WebClient.create(),
                baseUrl + tokenPath,
                "warehouse",
                "2SBrpOpWZJVIErZ4BvYFG9RFKOwIZxV0", // fake secret
                "solution4europe",
                "solution4europe"
        );

        MockResponse mockResponse = new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(load());
        mockWebServer.enqueue(mockResponse);

        Token token = oidcAdminClient.token();

        assertThat(token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
    }


    private String load() throws IOException {

        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("test-token.json");
        assert inputStream != null;

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

}