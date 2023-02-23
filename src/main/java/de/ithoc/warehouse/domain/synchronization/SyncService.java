package de.ithoc.warehouse.domain.synchronization;

import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.OidcTokenClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.epages.schema.orders.BillingAddress;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.persistence.entities.Client;
import de.ithoc.warehouse.persistence.entities.SyncEntity;
import de.ithoc.warehouse.persistence.entities.SyncHistory;
import de.ithoc.warehouse.persistence.entities.Warehouse;
import de.ithoc.warehouse.persistence.repositories.ClientRepository;
import de.ithoc.warehouse.persistence.repositories.SyncEntityRepository;
import de.ithoc.warehouse.persistence.repositories.SyncHistoryRepository;
import de.ithoc.warehouse.persistence.repositories.WarehouseRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.keycloak.models.UserModel;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    private final SyncEntityRepository syncEntityRepository;
    private final SyncHistoryRepository syncHistoryRepository;

    public SyncService(EpagesClient epagesClient, OidcTokenClient oidcTokenClient, OidcAdminClient oidcAdminClient,
                       WarehouseRepository warehouseRepository, ClientRepository clientRepository,
                       SyncEntityRepository syncEntityRepository, SyncHistoryRepository syncHistoryRepository) {
        this.epagesClient = epagesClient;
        this.oidcTokenClient = oidcTokenClient;
        this.oidcAdminClient = oidcAdminClient;
        this.warehouseRepository = warehouseRepository;
        this.clientRepository = clientRepository;
        this.syncEntityRepository = syncEntityRepository;
        this.syncHistoryRepository = syncHistoryRepository;
    }


    @Transactional
    public void syncOrdersAndCustomers() {

        LocalDateTime now = LocalDateTime.now();

        /*
         * Load the last loading timestamp to filter orders that have been delivered since last load.
         */
        List<Item> orderItems = flattenOrderItems();

        /*
         * Get the customers from the orders and check if they need to be added to the authorization server.
         */
        List<User> users = checkForUsers(orderItems);
        List<Client> clients = checkForClientAndWarehouse(users);

        /*
         * Update warehouse stocks using quantities from new orders.
         */
        updateStocks();

        /*
         * This current datetime is used to save it to the loading history. It will be
         * used for next orders load.
         */
        updateSyncHistory(now);
    }

    private void updateStocks() {
    }


    List<Client> checkForClientAndWarehouse(List<User> users) {
        List<Client> clients = new ArrayList<>();
        users.forEach(user -> {
            String companyName = company(user);
            Client client = clientRepository.findByName(companyName).orElseGet(() -> {
                Warehouse newWarehouse = new Warehouse();
                newWarehouse.setName(companyName); // defaults to company name
                newWarehouse = warehouseRepository.save(newWarehouse);

                Client newClient = new Client();
                newClient.setName(companyName); // defaults to company name
                newClient.setWarehouses(List.of(newWarehouse));
                newClient = clientRepository.save(newClient);

                return newClient;
            });
            clients.add(client);
        });
        log.debug("clients: {}", clients);

        return clients;
    }


    String company(User user) {
        if(user.getAttributes() == null ||
                user.getAttributes().getCompany() == null ||
                user.getAttributes().getCompany().size() == 0) {
            // Set username as default company name.
            return user.getUsername();
        }

        if(user.getAttributes().getCompany().size() == 1) {
            return user.getAttributes().getCompany().get(0);
        } else {
            List<String> company = user.getAttributes().getCompany();
            String message = "Multiple companies exist for '"+ company.get(0) +"': " + company.size();
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


    List<Item> flattenOrderItems() {

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
