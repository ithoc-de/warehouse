package de.ithoc.warehouse.domain.synchronization;

import de.ithoc.warehouse.domain.mapper.OidcUserMapper;
import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.OidcTokenClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.epages.schema.orders.BillingAddress;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.external.epages.schema.orders.order.Order;
import de.ithoc.warehouse.persistence.entities.Package;
import de.ithoc.warehouse.persistence.entities.*;
import de.ithoc.warehouse.persistence.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.keycloak.models.UserModel;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SyncService {

    private final EpagesClient epagesClient;
    private final OidcTokenClient oidcTokenClient;
    private final OidcAdminClient oidcAdminClient;
    private final WarehouseRepository warehouseRepository;
    private final ClientRepository clientRepository;
    private final PackageRepository packageRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final SyncEntityRepository syncEntityRepository;
    private final SyncHistoryRepository syncHistoryRepository;

    public SyncService(EpagesClient epagesClient, OidcTokenClient oidcTokenClient, OidcAdminClient oidcAdminClient,
                       WarehouseRepository warehouseRepository, ClientRepository clientRepository,
                       PackageRepository packageRepository, ProductRepository productRepository, StockRepository stockRepository, SyncEntityRepository syncEntityRepository, SyncHistoryRepository syncHistoryRepository) {
        this.epagesClient = epagesClient;
        this.oidcTokenClient = oidcTokenClient;
        this.oidcAdminClient = oidcAdminClient;
        this.warehouseRepository = warehouseRepository;
        this.clientRepository = clientRepository;
        this.packageRepository = packageRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.syncEntityRepository = syncEntityRepository;
        this.syncHistoryRepository = syncHistoryRepository;
    }


    @Transactional
    public void syncQuantities() {

        LocalDateTime timestamp = LocalDateTime.now();

        /*
         * Load the last loading timestamp to filter orders that have been delivered since last load.
         */
        List<Item> orderItems = loadNewOrderItems();
        List<Order> orders = loadNewOrders(orderItems);

        orders.forEach(order -> {
            String clientName = order.getBillingAddress().getCompany();
            if(clientName == null) {
                clientName = order.getBillingAddress().getFirstName() + " " + order.getBillingAddress().getLastName();
            }

            Client client = checkForClient(clientName);
            if(client.getWarehouses() == null || client.getWarehouses().isEmpty()) {
                Warehouse warehouse = checkForWarehouse(clientName);
                List<Warehouse> warehouses = new ArrayList<>();
                warehouses.add(warehouse);
                client.setWarehouses(warehouses);
                clientRepository.save(client);
            }

            order.getLineItemContainer().getProductLineItems().forEach(productLineItem -> {
                Product product = checkForProduct(productLineItem.getProductId(), productLineItem.getName());
                Package aPackage = checkForPackage(product);
                if(aPackage.getProducts() == null || aPackage.getProducts().size() == 0) {
                    List<Product> products = new ArrayList<>();
                    products.add(product);
                    aPackage.setProducts(products);
                    packageRepository.save(aPackage);
                }

                /*
                 * Update warehouse stocks using quantities from new orders.
                 */
                product = checkForStocks(product);
                List<Stock> stocks = product.getStocks();
                Comparator<Stock> comparatorDesc =
                        (stock1, stock2) -> stock2.getValidFrom().compareTo(stock1.getValidFrom());
                stocks.sort(comparatorDesc);
                Long quantity = stocks.get(0).getQuantity();
                quantity += productLineItem.getQuantity().getAmount();

                Stock stock = new Stock();
                stock.setValidFrom(timestamp);
                stock.setQuantity(quantity);
                stockRepository.save(stock);

                stocks.add(stock);
                product.setStocks(stocks);
                productRepository.save(product);
            });
        });

        /*
         * Get the customers from the orders and check if they need to be added to the authorization server.
         */
        List<User> users = checkForUsers(orderItems);

        /*
         * This current datetime is used to save it to the loading history. It will be
         * used for next orders load.
         */
        updateSyncHistory(timestamp);
    }


    private void updateStocks(List<Order> orders) {
    }


    Product checkForProduct(String productId, String productName) {
        Optional<Product> productOptional = productRepository.findByExternalId(productId);
        if (productOptional.isEmpty()) {
            Product newProduct = new Product();
            newProduct.setExternalId(productId);
            newProduct.setName(productName);
            return productRepository.save(newProduct);
        }
        Product product = productOptional.get();
        log.debug("product: {}", product);

        return product;
    }


    Product checkForStocks(Product product) {
        if(product.getStocks() == null || product.getStocks().size() == 0) {
            Stock stock = new Stock();
            stock.setQuantity(0L);
            stock.setValidFrom(LocalDateTime.of(
                    2020, Month.JANUARY, 1, 0, 0, 0, 0));
            stockRepository.save(stock);
            product.setStocks(new ArrayList<>());
            product.getStocks().add(stock);
            productRepository.save(product);
        }

        return product;
    }

    Package checkForPackage(Product product) {

        Optional<Package> packageOpt = packageRepository.findByName(product.getName());
        if (packageOpt.isEmpty()) {
            Package newPackage = new Package();
            newPackage.setName(product.getName());
            newPackage.setProducts(List.of(product));
            packageOpt = Optional.of(packageRepository.save(newPackage));
        }
        Package aPackage = packageOpt.get();
        log.debug("package: {}", aPackage);

        return aPackage;
    }


    /**
     * Load single orders by their order IDs taken from the order items.
     *
     * @param orderItems Order items loaded from shop.
     * @return All orders which contain customer and quantities.
     */
    List<Order> loadNewOrders(List<Item> orderItems) {

        List<Order> orders = orderItems.stream()
                .map(orderItem -> epagesClient.order(orderItem.getOrderId()))
                .toList();
        log.debug("orders: {}", orders);

        return orders;
    }


    Client checkForClient(String clientName) {
        Optional<Client> clientOptional = clientRepository.findByName(clientName);
        if(clientOptional.isEmpty()) {
            Client newClient = new Client();
            newClient.setName(clientName); // defaults to company name
            newClient = clientRepository.save(newClient);
            clientOptional = Optional.of(newClient);
        }
        Client client = clientOptional.get();
        log.debug("client: {}", client);

        return client;
    }


    Warehouse checkForWarehouse(String warehouseName) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByName(warehouseName);
        if(warehouseOptional.isEmpty()) {
            Warehouse newWarehouse = new Warehouse();
            newWarehouse.setName(warehouseName); // defaults to company name
            newWarehouse = warehouseRepository.save(newWarehouse);
            warehouseOptional = Optional.of(newWarehouse);
        }
        Warehouse warehouse = warehouseOptional.get();
        log.debug("warehouse: {}", warehouse);

        return warehouse;
    }


    String company(User user) {
        if (user.getAttributes() == null ||
                user.getAttributes().getCompany() == null ||
                user.getAttributes().getCompany().size() == 0) {
            // Set username as default company name.
            return user.getUsername();
        }

        if (user.getAttributes().getCompany().size() == 1) {
            return user.getAttributes().getCompany().get(0);
        } else {
            List<String> company = user.getAttributes().getCompany();
            String message = "Multiple companies exist for '" + company.get(0) + "': " + company.size();
            log.error(message);
            throw new MultipleOAuth2AttributesException(message);
        }
    }


    void updateSyncHistory(LocalDateTime now) {
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(now, ZoneId.systemDefault());
        LocalDateTime utc = zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        SyncHistory syncHistory = new SyncHistory();
        Optional<SyncEntity> syncEntity = syncEntityRepository.findByName("Orders");
        if (syncEntity.isEmpty()) {
            String message = "No sync entity 'Orders' found.";
            log.error(message);
            throw new RecordNotFoundException(message);
        }
        syncHistory.setSyncEntity(syncEntity.get());
        syncHistory.setTimestamp(utc);
        syncHistoryRepository.save(syncHistory);
    }


    List<User> checkForUsers(List<Item> filteredItems) {
        List<User> users = new ArrayList<>();
        filteredItems.forEach(item -> {
            BillingAddress billingAddress = item.getBillingAddress();
            String company = billingAddress.getCompany();
            String emailAddress = billingAddress.getEmailAddress();
            String firstName = billingAddress.getFirstName();
            String lastName = billingAddress.getLastName();

            Token token = oidcTokenClient.token();
            User user = oidcAdminClient.getUserBy(emailAddress, token).orElseGet(() -> {
                String requiredAction = oidcAdminClient.requiredActionAsString(UserModel.RequiredAction.UPDATE_PASSWORD);
                UserInput userInput = createUserInput(emailAddress, firstName, lastName, company, requiredAction);
                oidcAdminClient.postUser(userInput, token);
                log.debug("userInput: {}:", userInput);
                OidcUserMapper oidcUserMapper = Mappers.getMapper(OidcUserMapper.class);
                return oidcUserMapper.toUser(userInput);
            });
            log.debug("user: {}", user);
            users.add(user);
        });
        log.debug("users: {}", users);

        return users;
    }


    List<Item> loadNewOrderItems() {

        Optional<SyncHistory> previousSyncHistory = syncHistoryRepository.findTopByOrderByTimestampDesc();
        if (previousSyncHistory.isEmpty()) {
            String message = "No previous sync history found. Insert at least one record to the database.";
            log.error(message);
            throw new RecordNotFoundException(message);
        }
        List<Item> filteredItems = epagesClient.orderItems(previousSyncHistory.get().getTimestamp());
        log.debug("filteredItems: {}", filteredItems);

        return filteredItems;
    }


    @NotNull
    static UserInput createUserInput(String emailAddress,
                                     String firstName, String lastName,
                                     String company, String requiredAction) {

        UserInput userInput = new UserInput();

        userInput.setUsername(emailAddress);
        userInput.setEmail(emailAddress);
        userInput.setFirstName(firstName);
        userInput.setLastName(lastName);
        userInput.setEmailVerified(Boolean.TRUE);
        userInput.setEnabled(Boolean.TRUE);
        userInput.setCompany(company);
        userInput.setRequiredActions(List.of(requiredAction));

        return userInput;
    }

}
