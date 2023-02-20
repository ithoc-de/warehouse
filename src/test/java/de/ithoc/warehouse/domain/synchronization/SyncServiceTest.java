package de.ithoc.warehouse.domain.synchronization;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import de.ithoc.warehouse.persistence.SyncEntity;
import de.ithoc.warehouse.persistence.SyncEntityRepository;
import de.ithoc.warehouse.persistence.SyncHistory;
import de.ithoc.warehouse.persistence.SyncHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private EpagesClient epagesClient;

    @Mock
    private OidcAdminClient oidcAdminClient;

    @Mock
    private SyncEntityRepository syncEntityRepository;
    @Mock
    private SyncHistoryRepository syncHistoryRepository;

    @InjectMocks
    private SyncService syncService;


    @Test
    void syncOrdersAndCustomers() {
    }


    @Test
    public void loadOrdersOnePage() throws IOException {
        Orders orders = loadTestOrders();
        LocalDateTime deliverDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 14,
                8, 10, 0, 0);
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(deliverDateTime, ZoneId.systemDefault());
        LocalDateTime deliverUtc = zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String deliverUtcStr = formatter.format(deliverUtc);

        orders.getItems().forEach(item -> item.setDeliveredOn(deliverUtcStr));

        Optional<SyncHistory> lastLoadingHistory = Optional.of(new SyncHistory());
        lastLoadingHistory.get().setTimestamp(deliverUtc.minusDays(3));
        when(syncHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(lastLoadingHistory);

        when(epagesClient.orders(any(LocalDateTime.class))).thenReturn(orders.getItems());
        when(oidcAdminClient.token()).thenReturn(new Token());
        when(oidcAdminClient.getUserBy(anyString(), any(Token.class))).thenReturn(Optional.of(new User()));
        when(syncEntityRepository.findByName("Orders")).thenReturn(Optional.of(new SyncEntity()));

        syncService.syncOrdersAndCustomers();

        verify(syncHistoryRepository, times(1)).findTopByOrderByTimestampDesc();
        verify(syncHistoryRepository, times(1)).save(any(SyncHistory.class));
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