package de.ithoc.warehouse.external.epages;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.RestClient;
import de.ithoc.warehouse.external.schema.orders.Item;
import de.ithoc.warehouse.external.schema.orders.Orders;
import de.ithoc.warehouse.persistence.CustomerRepository;
import de.ithoc.warehouse.persistence.LoadingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdersLoaderTest {

    @Mock
    private LoadingHistoryRepository loadingHistoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private OrdersLoader ordersLoader;

    @Test
    public void zonedToLocalDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2023, 2, 12,
                6, 14, 1, 0, ZoneId.of("CET"));
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

        assertEquals("2023-02-12T06:14:01", localDateTime.toString());
    }
    @Test
    public void loadItemsOnePage() throws IOException {
        Orders orders = loadTestOrders();
        Map<String, String> variables = Map.of(
                "updatedFrom", "2023-02-12T00:00:00.000Z",
                "deliveredOn", "true",
                "page", "1"
        );
        when(restClient.get(eq("/orders"), eq(variables), ArgumentMatchers.<Class<Orders>>any())).thenReturn(orders);

        LocalDateTime updatedFromZonedDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY.getValue(), 12,
                0, 0, 0, 0);

        List<Item> items = ordersLoader.loadItems(updatedFromZonedDateTime);

        verify(restClient, times(1))
                .get(eq("/orders"), eq(variables), ArgumentMatchers.<Class<Orders>>any());
        assertEquals(5, items.size());
    }

    @Test()
    public void loadItemsWithZonedDateTimeOtherThanUtc() {

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
