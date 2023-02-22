package de.ithoc.warehouse.external.epages;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.epages.schema.customers.Customers;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class EpagesClientTest {

    private MockWebServer mockWebServer;
    private EpagesClient epagesClient;

    private static final String apiKey = "dYjVPxUmo8QG5RVe3agxhlJRd04P3SWF"; // fake api key

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        int port = mockWebServer.getPort();
        String baseUrl = "http://localhost:" + port;
        WebClient webClient = WebClient.create();
        epagesClient = new EpagesClient(webClient, baseUrl, apiKey);
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }


    @Test
    public void getCustomers() throws IOException, InterruptedException {
        String authorization = "Bearer " + apiKey;
        Customers expectedCustomers = loadTestCustomers();
        ObjectMapper objectMapper = new ObjectMapper();
        String customersStr = objectMapper.writeValueAsString(expectedCustomers);

        MockResponse mockResponse = new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setHeader(HttpHeaders.AUTHORIZATION, authorization)
                .setBody(customersStr);
        mockWebServer.enqueue(mockResponse);

        Customers customers = epagesClient.getCustomers();

        assertThat(customers.getItems().get(0).getCustomerId())
                .isEqualTo("632BA7E5-18CB-D0E1-9397-0A0C05B4CD37");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String body = recordedRequest.toString();
        assertThat(body).contains("GET /customers HTTP/1.1");
    }


    @Test
    public void getOrderItems() throws IOException {

        String authorization = "Bearer " + apiKey;
        Orders expectedOrders = loadTestOrders();
        ObjectMapper objectMapper = new ObjectMapper();
        String ordersStr = objectMapper.writeValueAsString(expectedOrders);

        MockResponse mockResponse = new MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setHeader(HttpHeaders.AUTHORIZATION, authorization)
                .setBody(ordersStr);
        mockWebServer.enqueue(mockResponse);

        LocalDateTime localDateTime = LocalDateTime.of(
                2020, Month.JANUARY, 1, 0, 0, 0, 0);
        Orders orders = epagesClient.orders(localDateTime, 1);

        assertThat(orders.getItems().get(0).getCustomerId())
                .isEqualTo("632BA7E6-2F21-C482-1565-0A0C05B41001");
        assertThat(orders.getItems().get(0).getBillingAddress().getEmailAddress())
                .isEqualTo("RobertLees@einrot.com");
        assertThat(orders.getItems().get(0).getDeliveredOn())
                .isEqualTo("2023-02-13T21:48:36.000Z");
    }


    @Test
    public void mapEpagesDateToJava() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dateString = "2015-11-03T08:48:26Z";
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);

        assertEquals(2015, dateTime.getYear());
    }

    @Test
    public void mapJavaDateToEpages() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime localDateTime = LocalDateTime.of(2023, Month.FEBRUARY, 12, 21, 48, 36, 0);
        String strDateTime = formatter.format(localDateTime);

        assertEquals("2023-02-12T21:48:36.000Z", strDateTime);
    }


    private Customers loadTestCustomers() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("test-customers.json");
        assert inputStream != null;
        String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Customers.class);
    }

    private Orders loadTestOrders() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("test-orders.json");
        assert inputStream != null;
        String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Orders.class);
    }

}