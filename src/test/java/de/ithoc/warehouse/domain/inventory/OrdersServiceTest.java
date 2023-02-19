package de.ithoc.warehouse.domain.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.epages.OrdersLoader;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import de.ithoc.warehouse.persistence.LoadingHistory;
import de.ithoc.warehouse.persistence.LoadingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdersServiceTest {

    @Mock
    private OrdersLoader ordersLoader;

    @Mock
    private LoadingHistoryRepository loadingHistoryRepository;

    @InjectMocks
    private OrdersService ordersService;


    @Test
    public void loadOrdersOnePage() throws IOException {
        Orders orders = loadTestOrders();
        LocalDateTime deliverDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 14,
                8, 10, 0, 0);
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(deliverDateTime, ZoneId.systemDefault());
        LocalDateTime deliverUtc = zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        String dateTime = ordersService.formatDateTime(deliverUtc);
        orders.getItems().forEach(item -> item.setDeliveredOn(dateTime));

        Optional<LoadingHistory> lastLoadingHistory = Optional.of(new LoadingHistory());
        lastLoadingHistory.get().setTimestamp(deliverUtc.minusDays(3));
        when(loadingHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(lastLoadingHistory);

        when(ordersLoader.loadItems(any(LocalDateTime.class))).thenReturn(orders.getItems());

        ordersService.updateInventory();

        verify(loadingHistoryRepository, times(1)).save(any(LoadingHistory.class));
        verify(loadingHistoryRepository, times(1)).findTopByOrderByTimestampDesc();
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
