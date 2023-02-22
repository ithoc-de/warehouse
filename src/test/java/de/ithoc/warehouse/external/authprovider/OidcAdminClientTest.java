package de.ithoc.warehouse.external.authprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.domain.synchronization.MultipleOAuth2UsersExeption;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.Dispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class OidcAdminClientTest {

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
    void getUsers() throws IOException {
        String baseUrl = "http://localhost:" + mockWebServer.getPort();

        OidcAdminClient oidcAdminClient = new OidcAdminClient(
                WebClient.create(),
                baseUrl + "/admin/realms/Solution4Europe"
        );

        MockResponse mockResponse = new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(load("test-users.json"));
        mockWebServer.enqueue(mockResponse);

        List<User> users = oidcAdminClient.getUsers(
                new ObjectMapper().readValue(load("test-token.json"), Token.class));

        assertThat(users.get(0).getEmail()).isEqualTo("oliver.hock@example.com");
    }


    @Test
    void getUserBy() throws IOException {
        String baseUrl = "http://localhost:" + mockWebServer.getPort();

        OidcAdminClient oidcAdminClient = new OidcAdminClient(
                WebClient.create(),
                baseUrl + "/admin/realms/Solution4Europe"
        );

        String userStr = load("test-user.json");
        MockResponse mockResponse = new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(userStr);
        mockWebServer.enqueue(mockResponse);

        Token token = new ObjectMapper().readValue(load("test-token.json"), Token.class);
        Optional<User> user = oidcAdminClient.getUserBy("oliver.hock@example.com", token);

        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getEmail()).isEqualTo("oliver.hock@example.com");
    }


    @Test
    void getUserByAmbiguousUsers() throws IOException {
        String baseUrl = "http://localhost:" + mockWebServer.getPort();

        OidcAdminClient oidcAdminClient = new OidcAdminClient(
                WebClient.create(),
                baseUrl + "/admin/realms/Solution4Europe"
        );

        String userStr = load("test-users.json");
        MockResponse mockResponse = new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(userStr);
        mockWebServer.enqueue(mockResponse);

        Token token = new ObjectMapper().readValue(load("test-token.json"), Token.class);

        assertThrows(MultipleOAuth2UsersExeption.class, () -> {
            Optional<User> user = oidcAdminClient.getUserBy("oliver.hock@example.com", token);
        });
    }


    @Test
    void getUserByEmpty() throws IOException {
        String baseUrl = "http://localhost:" + mockWebServer.getPort();

        OidcAdminClient oidcAdminClient = new OidcAdminClient(
                WebClient.create(),
                baseUrl + "/admin/realms/Solution4Europe"
        );

        String emptyStr = load("test-empty.json");
        MockResponse mockResponse = new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(emptyStr);
        mockWebServer.enqueue(mockResponse);

        Token token = new ObjectMapper().readValue(load("test-token.json"), Token.class);
        Optional<User> user = oidcAdminClient.getUserBy("oliver.hock@example.com", token);

        assertThat(user).isEmpty();
    }


    @Test
    void postUser() throws IOException, InterruptedException {
        String baseUrl = "http://localhost:" + mockWebServer.getPort();

        OidcAdminClient oidcAdminClient = new OidcAdminClient(
                WebClient.create(),
                baseUrl + "/admin/realms/Solution4Europe"
        );

        String location = "http://localhost:7002/admin/realms/Solution4Europe/users/7453d0c9-f0af-4a48-811d-d069ee90ba34";
        MockResponse mockResponse = new MockResponse().setResponseCode(201)
                .setHeader("Location", location);
        mockWebServer.enqueue(mockResponse);

        UserInput userInput = new UserInput();
        userInput.setUsername("user.name@example.com");
        userInput.setEmail("user.name@example.com");
        userInput.setEmailVerified(true);
        userInput.setFirstName("User");
        userInput.setLastName("Name");
        userInput.setEnabled(true);
        userInput.setRequiredActions(List.of("UPDATE_PASSWORD"));

        Token token = new ObjectMapper().readValue(load("test-token.json"), Token.class);

        Void unused = oidcAdminClient.postUser(userInput, token);
        assertThat(unused).isNull();

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assert recordedRequest != null;
        String authorization = recordedRequest.getHeader("Authorization");
        assertThat(authorization).isEqualTo("Bearer " + ACCESS_TOKEN);
    }


    private String load(String fileName) throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(fileName);
        assert inputStream != null;

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

}