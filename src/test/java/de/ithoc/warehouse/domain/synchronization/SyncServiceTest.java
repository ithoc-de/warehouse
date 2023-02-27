package de.ithoc.warehouse.domain.synchronization;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.OidcTokenClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.Attributes;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
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
    public void loadNewOrders() {
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
        when(epagesClient.orderItems(any(LocalDateTime.class))).thenReturn(items);

        SyncHistory syncHistory = new SyncHistory();
        syncHistory.setTimestamp(LocalDateTime.now());
        List<Order> orders = syncService.loadNewOrders(syncHistory);

        assertThat(orders.size()).isEqualTo(n);
        for (int i = 0; i < n; i++) {
            assertThat(orders.get(i).getOrderId()).isEqualTo("" + (i + 1));
        }

        verify(epagesClient, times(1)).orderItems(any(LocalDateTime.class));
        verify(epagesClient, times(n)).order(anyString());
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
    public void loadLastSyncHistory() {
        SyncEntity syncEntity = new SyncEntity();
        syncEntity.setName("Orders");
        syncEntity.setTimestampField("deliveredOn");
        when(syncEntityRepository.findByName(eq("Orders"))).thenReturn(Optional.of(syncEntity));

        SyncHistory syncHistory = new SyncHistory();
        syncHistory.setTimestamp(LocalDateTime.now());
        syncHistory.setSyncEntity(syncEntity);
        when(syncHistoryRepository.findTopBySyncEntityOrderByTimestampDesc(any(SyncEntity.class)))
                .thenReturn(Optional.of(syncHistory));

        SyncHistory actualSyncHistory = syncService.loadLastSyncHistory();

        assertThat(actualSyncHistory).isNotNull();
        assertThat(actualSyncHistory.getTimestamp()).isNotNull();
        assertThat(actualSyncHistory.getSyncEntity()).isNotNull();
        assertThat(actualSyncHistory.getSyncEntity().getName()).isEqualTo("Orders");

        verify(syncEntityRepository, times(1)).findByName(anyString());
        verify(syncHistoryRepository, times(1))
                .findTopBySyncEntityOrderByTimestampDesc(any(SyncEntity.class));
    }


    @Test
    public void loadLastSyncHistoryEntityException() {
        when(syncEntityRepository.findByName(eq("Orders"))).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> {
            syncService.loadLastSyncHistory();
        });

        verify(syncEntityRepository, times(1)).findByName(anyString());
        verify(syncHistoryRepository, times(0))
                .findTopBySyncEntityOrderByTimestampDesc(any(SyncEntity.class));
    }


    @Test
    public void loadLastSyncHistoryHistoryException() {
        SyncEntity syncEntity = new SyncEntity();
        syncEntity.setName("Orders");
        syncEntity.setTimestampField("deliveredOn");
        when(syncEntityRepository.findByName(eq("Orders"))).thenReturn(Optional.of(syncEntity));

        when(syncHistoryRepository.findTopBySyncEntityOrderByTimestampDesc(any(SyncEntity.class)))
                .thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> {
            syncService.loadLastSyncHistory();
        });

        verify(syncEntityRepository, times(1)).findByName(anyString());
        verify(syncHistoryRepository, times(1))
                .findTopBySyncEntityOrderByTimestampDesc(any(SyncEntity.class));
    }


    @Test
    public void checkForUser() throws IOException {
        Order order = loadTestOrder();
        User user = new User();
        user.setEmail("MadisonGough@einrot.com");

        when(oidcTokenClient.token()).thenReturn(new Token());
        when(oidcAdminClient.getUserBy(eq(user.getEmail()), any(Token.class))).thenReturn(Optional.of(user));

        User actualUser = syncService.checkForUser(order);

        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getEmail()).isEqualTo("MadisonGough@einrot.com");
        verify(oidcTokenClient, times(1)).token();
        verify(oidcAdminClient, times(0))
                .requiredActionAsString(any(UserModel.RequiredAction.class));
        verify(oidcAdminClient, times(0)).postUser(any(User.class), any(Token.class));
    }


    @Test
    public void checkCreateUser() throws IOException {
        Order order = loadTestOrder();
        User user = new User();
        user.setEmail("MadisonGough@einrot.com");

        when(oidcTokenClient.token()).thenReturn(new Token());
        when(oidcAdminClient.getUserBy(eq(user.getEmail()), any(Token.class))).thenReturn(Optional.empty());
        when(oidcAdminClient.requiredActionAsString(any(UserModel.RequiredAction.class)))
                .thenReturn("UPDATE_PASSWORD");
        when(oidcAdminClient.postUser(any(User.class), any(Token.class))).thenReturn(user);

        User actualUser = syncService.checkForUser(order);

        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getEmail()).isEqualTo("MadisonGough@einrot.com");
        verify(oidcTokenClient, times(1)).token();
        verify(oidcAdminClient, times(1))
                .requiredActionAsString(any(UserModel.RequiredAction.class));
        verify(oidcAdminClient, times(1)).postUser(any(User.class), any(Token.class));
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
        when(syncHistoryRepository.findTopBySyncEntityOrderByTimestampDesc(any(SyncEntity.class))).thenReturn(lastLoadingHistory);

        when(epagesClient.orderItems(any(LocalDateTime.class))).thenReturn(orders.getItems());
        when(oidcTokenClient.token()).thenReturn(new Token());
        when(oidcAdminClient.getUserBy(anyString(), any(Token.class))).thenReturn(Optional.of(new User()));
        when(syncEntityRepository.findByName("Orders")).thenReturn(Optional.of(new SyncEntity()));

        syncService.syncQuantities();

        verify(syncHistoryRepository, times(1))
                .findTopBySyncEntityOrderByTimestampDesc(any(SyncEntity.class));
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


    @Test
    public void loadProduct() {
        String productId = "PRODUCT-ID";
        de.ithoc.warehouse.external.epages.schema.products.product.Product product =
                new de.ithoc.warehouse.external.epages.schema.products.product.Product();
        product.setProductId(productId);
        product.setProductNumber("1001");
        when(epagesClient.product(eq(productId))).thenReturn(product);

        de.ithoc.warehouse.external.epages.schema.products.product.Product actualProduct =
                syncService.loadProduct(productId);

        assertThat(actualProduct.getProductId()).isEqualTo(productId);
        assertThat(actualProduct.getProductNumber()).isEqualTo("1001");
    }

    private Orders loadTestOrders() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("test-orders.json");
        assert inputStream != null;
        String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Orders.class);
    }


    private Order loadTestOrder() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("test-order.json");
        assert inputStream != null;
        String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Order.class);
    }

}