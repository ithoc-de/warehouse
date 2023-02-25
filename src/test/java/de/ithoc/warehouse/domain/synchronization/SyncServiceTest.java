package de.ithoc.warehouse.domain.synchronization;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.domain.mapper.OidcUserMapper;
import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.OidcTokenClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.Attributes;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import de.ithoc.warehouse.external.epages.schema.orders.order.Order;
import de.ithoc.warehouse.persistence.entities.Package;
import de.ithoc.warehouse.persistence.entities.*;
import de.ithoc.warehouse.persistence.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.UserModel;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private ClientRepository clientRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SyncEntityRepository syncEntityRepository;

    @Mock
    private SyncHistoryRepository syncHistoryRepository;

    @InjectMocks
    private SyncService syncService;


    @Test
    void syncQuantities() {
    }


    @Test
    public void checkForProduct() {
        Product product = new Product();
        product.setExternalId("ID");
        product.setName("Name");

        when(productRepository.findByExternalId(anyString())).thenReturn(Optional.of(product));

        Product actualProduct = syncService.checkForProduct("ID", "Name");

        assertThat(product).isNotNull();

        verify(productRepository, times(1)).findByExternalId(anyString());
        verify(productRepository, times(0)).save(any(Product.class));
    }

    @Test
    public void checkForProductEmpty() {
        Product product = new Product();
        product.setExternalId("ID");
        product.setName("Name");

        when(productRepository.findByExternalId(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product actualProduct = syncService.checkForProduct("ID", "Name");

        assertThat(product).isNotNull();

        verify(productRepository, times(1)).findByExternalId(anyString());
        verify(productRepository, times(1)).save(any(Product.class));
    }


    @Test
    public void checkForStocks() {
        Product product = new Product();
        product.setStocks(new ArrayList<>());
        Stock stock = new Stock();
        product.getStocks().add(stock);

        Product actualProduct = syncService.checkForStocks(product);

        assertThat(actualProduct.getStocks().size()).isEqualTo(1);

        verify(stockRepository, times(0)).save(any(Stock.class));
        verify(productRepository, times(0)).save(any(Product.class));
    }


    @Test
    public void checkForStocksNull() {
        Product product = new Product();
        Stock stock = new Stock();

        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product actualProduct = syncService.checkForStocks(product);

        assertThat(actualProduct.getStocks().size()).isEqualTo(1);

        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }


    @Test
    public void checkForStocksEmpty() {
        Product product = new Product();
        product.setStocks(new ArrayList<>());
        Stock stock = new Stock();

        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product actualProduct = syncService.checkForStocks(product);

        assertThat(actualProduct.getStocks().size()).isEqualTo(1);

        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }


    @Test
    public void checkForPackage() {
        String name = "Package Name";
        Package aPackage = new Package();
        aPackage.setName(name);
        Product product = new Product();
        product.setName(name);
        List<Product> products = List.of(product);
        aPackage.setProducts(products);

        when(packageRepository.findByName(name)).thenReturn(Optional.of(aPackage));

        Package actualPackage = syncService.checkForPackage(products.get(0));

        assertThat(actualPackage.getName()).isEqualTo("Package Name");

        verify(packageRepository, times(1)).findByName(name);
        verify(packageRepository, times(0)).save(any(Package.class));
    }


    @Test
    public void checkForPackageEmpty() {
        String name = "Package Name";
        Package aPackage = new Package();
        aPackage.setName(name);
        Product product = new Product();
        product.setName(name);
        List<Product> products = List.of(product);
        aPackage.setProducts(products);

        when(packageRepository.findByName(name)).thenReturn(Optional.empty());
        when(packageRepository.save(any(Package.class))).thenReturn(aPackage);

        syncService.checkForPackage(products.get(0));

        verify(packageRepository, times(1)).findByName(name);
        verify(packageRepository, times(1)).save(any(Package.class));
    }


    @Test
    public void loadNewOrder() {
        int n = 3;
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String orderId = "" + (i + 1);

            Item item = new Item();
            item.setOrderId(orderId);
            items.add(item);

            Order order = new Order();
            order.setOrderId(orderId);
            when(epagesClient.order(orderId)).thenReturn(order);
        }

        List<Order> orders = syncService.loadNewOrders(items);

        assertThat(orders.size()).isEqualTo(n);
        for (int i = 0; i < n; i++) {
            assertThat(orders.get(i).getOrderId()).isEqualTo("" + (i + 1));
        }
    }


    @Test
    public void company() {
        List<String> companies = new ArrayList<>();
        companies.add("Company");
        Attributes attributes = new Attributes();
        attributes.setCompany(companies);
        User user = new User();
        user.setAttributes(attributes);

        String company = syncService.company(user);

        assertThat(company).isEqualTo("Company");
    }


    @Test
    public void companyAttributesNull() {
        User user = new User();
        user.setUsername("Username");

        String company = syncService.company(user);

        assertThat(company).isEqualTo("Username");
    }


    @Test
    public void companyCompanyEmpty() {
        User user = new User();
        user.setUsername("Username");
        Attributes attributes = new Attributes();
        user.setAttributes(attributes);

        String company = syncService.company(user);

        assertThat(company).isEqualTo("Username");
    }


    @Test
    public void companyCompanyMultiple() {
        User user = new User();
        user.setUsername("Username");
        Attributes attributes = new Attributes();
        attributes.setCompany(List.of("Company 1", "Company 2"));
        user.setAttributes(attributes);

        assertThrows(MultipleOAuth2AttributesException.class, () -> {
            syncService.company(user);
        });
    }


    @Test
    public void checkForClient() {
        Client client = new Client();
        client.setName("Name");
        when(clientRepository.findByName("Name")).thenReturn(Optional.of(client));

        Client actualClient = syncService.checkForClient("Name");

        assertThat(actualClient.getName()).isEqualTo("Name");
        verify(clientRepository, times(1)).findByName("Name");
        verify(clientRepository, times(0)).save(any(Client.class));
    }


    @Test
    public void checkForClientEmpty() {
        Client client = new Client();
        client.setName("Name");
        when(clientRepository.findByName("Name")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client actualClient = syncService.checkForClient("Name");

        assertThat(actualClient.getName()).isEqualTo("Name");
        verify(clientRepository, times(1)).findByName("Name");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    public void checkForWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Name");
        when(warehouseRepository.findByName("Name")).thenReturn(Optional.of(warehouse));

        Warehouse actualWarehouse = syncService.checkForWarehouse("Name");

        assertThat(actualWarehouse.getName()).isEqualTo("Name");
        verify(warehouseRepository, times(1)).findByName("Name");
        verify(warehouseRepository, times(0)).save(any(Warehouse.class));
    }


    @Test
    public void checkForWarehouseEmpty() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Name");
        when(warehouseRepository.findByName("Name")).thenReturn(Optional.empty());
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        Warehouse actualWarehouse = syncService.checkForWarehouse("Name");

        assertThat(actualWarehouse.getName()).isEqualTo("Name");
        verify(warehouseRepository, times(1)).findByName("Name");
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }


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
                "User", "Name", "Company", "UPDATE_PASSWORD");

        assertThat(userInput.getUsername()).isEqualTo("user.name@example.com");
        assertThat(userInput.getEmail()).isEqualTo("user.name@example.com");
        assertThat(userInput.getFirstName()).isEqualTo("User");
        assertThat(userInput.getLastName()).isEqualTo("Name");
        assertThat(userInput.getEmailVerified()).isEqualTo(true);
        assertThat(userInput.getEnabled()).isEqualTo(true);
        assertThat(userInput.getCompany()).isEqualTo("Company");
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

        List<Item> filteredItems = syncService.loadNewOrderItems();
        assertThat(filteredItems.size()).isGreaterThan(0);
    }


    @Test
    public void getFilteredItemsNoHistory() {
        when(syncHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> {
            syncService.loadNewOrderItems();
        });
    }


    @Test
    public void checkForUser() throws IOException {
        Orders orders = loadTestOrders();
        List<Item> filteredItems = orders.getItems();
        User user = new User();
        user.setEmail("RobertLees@einrot.com");

        when(oidcTokenClient.token()).thenReturn(new Token());
        when(oidcAdminClient.getUserBy(eq(user.getEmail()), any(Token.class))).thenReturn(Optional.of(user));

        List<User> users = syncService.checkForUsers(filteredItems);

        assertThat(users.size()).isEqualTo(5);
        verify(oidcTokenClient, times(5)).token();
        verify(oidcAdminClient, times(0))
                .requiredActionAsString(any(UserModel.RequiredAction.class));
        verify(oidcAdminClient, times(0))
                .postUser(any(UserInput.class), any(Token.class));
    }


    @Test
    public void checkCreateUser() throws IOException {
        Orders orders = loadTestOrders();
        List<Item> filteredItems = orders.getItems();
        User user = new User();
        user.setEmail("RobertLees@einrot.com");

        when(oidcTokenClient.token()).thenReturn(new Token());
        when(oidcAdminClient.getUserBy(eq(user.getEmail()), any(Token.class))).thenReturn(Optional.empty());
        when(oidcAdminClient.requiredActionAsString(any(UserModel.RequiredAction.class)))
                .thenReturn("UPDATE_PASSWORD");
        doNothing().when(oidcAdminClient).postUser(any(UserInput.class), any(Token.class));

        List<User> users = syncService.checkForUsers(filteredItems);

        assertThat(users.size()).isEqualTo(5);
        verify(oidcTokenClient, times(5)).token();
        verify(oidcAdminClient, times(5))
                .requiredActionAsString(any(UserModel.RequiredAction.class));
        verify(oidcAdminClient, times(5))
                .postUser(any(UserInput.class), any(Token.class));

    }


//    @Test
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

        syncService.syncQuantities();

        verify(syncHistoryRepository, times(1)).findTopByOrderByTimestampDesc();
        verify(syncHistoryRepository, times(1)).save(any(SyncHistory.class));
    }


    @Test
    public void updateSyncHistory() {
        SyncEntity syncEntity = new SyncEntity();
        syncEntity.setName("Orders");
        syncEntity.setTimestampField("deliveredOn");
        when(syncEntityRepository.findByName("Orders")).thenReturn(Optional.of(syncEntity));

        when(syncHistoryRepository.save(any(SyncHistory.class))).thenReturn(new SyncHistory());

        syncService.updateSyncHistory(LocalDateTime.now());

        verify(syncEntityRepository, times(1)).findByName("Orders");
        verify(syncHistoryRepository, times(1)).save(any(SyncHistory.class));
    }


    @Test
    public void updateSyncHistoryEmpty() {
        when(syncEntityRepository.findByName("Orders")).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> {
            syncService.updateSyncHistory(LocalDateTime.now());
        });
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