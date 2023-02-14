package de.ithoc.warehouse.external;

import de.ithoc.warehouse.external.schema.customers.Customers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestClient restClient;

    @Test
    void customersGet() {
        Customers mockedCustomers = new Customers();
        mockedCustomers.setResults(3L);
        ResponseEntity<Customers> responseEntity = new ResponseEntity<>(mockedCustomers, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<Customers>>any(), anyMap())
        ).thenReturn(responseEntity);

        Customers actualCustomers = restClient.get("/customers", Map.of(), Customers.class);

        assertNotNull(actualCustomers);
        assertEquals(3, actualCustomers.getResults());
    }

}