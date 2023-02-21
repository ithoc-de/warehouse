package de.ithoc.warehouse.external.epages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import de.ithoc.warehouse.external.epages.schema.customers.Customers;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EpagesClientTest {

    private WireMockServer wireMockServer;
    private WebClient webClient;

    @BeforeEach
    void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        webClient = WebClient.builder().baseUrl(wireMockServer.baseUrl()).build();
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

    @Test
    public void getCustomers() throws IOException {
        Customers customers = loadTestCustomers();
    }


    private Customers loadTestCustomers() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("test-customers.json");
        assert inputStream != null;
        String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Customers.class);
    }

}