package de.ithoc.warehouse.domain.synchronization;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.OidcTokenClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import de.ithoc.warehouse.persistence.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private EpagesClient epagesClient;

    @Mock
    private OidcTokenClient oidcTokenClient;

    @Mock
    private OidcAdminClient oidcAdminClient;

    @Mock
    private SyncEntityRepository syncEntityRepository;

    @Mock
    private SyncHistoryRepository syncHistoryRepository;

    @InjectMocks
    private SyncService syncService;


    @Test
    public void toUserInput() {
        User user = new User();
        user.setUsername("user.name@example.com");
        user.setEmail("user.name@example.com");
        user.setFirstName("User");
        user.setLastName("Name");
        user.setEmailVerified(true);
        user.setEnabled(Boolean.TRUE);
        user.setRequiredActions(List.of("UPDATE_PASSWORD"));

        OidcUserMapper oidcUserMapper = Mappers.getMapper(OidcUserMapper.class);
        UserInput userInput = oidcUserMapper.toUserInput(user);

        assertThat(userInput.getUsername()).isEqualTo("user.name@example.com");
        assertThat(userInput.getEmail()).isEqualTo("user.name@example.com");
        assertThat(userInput.getFirstName()).isEqualTo("User");
        assertThat(userInput.getLastName()).isEqualTo("Name");
        assertThat(userInput.getEmailVerified()).isEqualTo(true);
        assertThat(userInput.getEnabled()).isEqualTo(true);
        assertThat(userInput.getRequiredActions().get(0)).isEqualTo("UPDATE_PASSWORD");
    }


    @Test
    public void toUser() {
        UserInput userInput = new UserInput();
        userInput.setUsername("user.name@example.com");
        userInput.setEmail("user.name@example.com");
        userInput.setFirstName("User");
        userInput.setLastName("Name");
        userInput.setEmailVerified(true);
        userInput.setEnabled(Boolean.TRUE);
        userInput.setRequiredActions(List.of("UPDATE_PASSWORD"));

        OidcUserMapper oidcUserMapper = Mappers.getMapper(OidcUserMapper.class);
        User user = oidcUserMapper.toUser(userInput);

        assertThat(user.getUsername()).isEqualTo("user.name@example.com");
        assertThat(user.getEmail()).isEqualTo("user.name@example.com");
        assertThat(user.getFirstName()).isEqualTo("User");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getEmailVerified()).isEqualTo(true);
        assertThat(user.getEnabled()).isEqualTo(true);
        assertThat(user.getRequiredActions().get(0)).isEqualTo("UPDATE_PASSWORD");
    }


    @Test
    public void createUserInput() {
        UserInput userInput = SyncService.createUserInput("user.name@example.com",
                "User", "Name", "UPDATE_PASSWORD");

        assertThat(userInput.getUsername()).isEqualTo("user.name@example.com");
        assertThat(userInput.getEmail()).isEqualTo("user.name@example.com");
        assertThat(userInput.getFirstName()).isEqualTo("User");
        assertThat(userInput.getLastName()).isEqualTo("Name");
        assertThat(userInput.getEmailVerified()).isEqualTo(true);
        assertThat(userInput.getEnabled()).isEqualTo(true);
        assertThat(userInput.getRequiredActions().get(0)).isEqualTo("UPDATE_PASSWORD");
    }


    @Test
    public void getFilteredItems() throws IOException {
        SyncHistory syncHistory = new SyncHistory();
        syncHistory.setTimestamp(LocalDateTime.now());
        when(syncHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(Optional.of(syncHistory));

        Orders orders = loadTestOrders();
        List<Item> filteredItemsMock = orders.getItems();
        when(epagesClient.orderItems(any(LocalDateTime.class))).thenReturn(filteredItemsMock);

        List<Item> filteredItems = syncService.getFilteredItems();
        assertThat(filteredItems.size()).isGreaterThan(0);
    }


    @Test
    public void getFilteredItemsNoHistory() {
        when(syncHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> {
            syncService.getFilteredItems();
        });
    }


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

        when(epagesClient.orderItems(any(LocalDateTime.class))).thenReturn(orders.getItems());
        when(oidcTokenClient.token()).thenReturn(new Token());
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