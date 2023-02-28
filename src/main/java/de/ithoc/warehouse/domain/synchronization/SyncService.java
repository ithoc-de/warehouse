package de.ithoc.warehouse.domain.synchronization;

import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.OidcTokenClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.authprovider.schema.users.UserInput;
import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.epages.schema.orders.BillingAddress;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.keycloak.models.UserModel;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SyncService {

    private final EpagesClient epagesClient;
    private final OidcTokenClient oidcTokenClient;
    private final OidcAdminClient oidcAdminClient;
    private final SyncEntityRepository syncEntityRepository;
    private final SyncHistoryRepository syncHistoryRepository;

    public SyncService(EpagesClient epagesClient, OidcTokenClient oidcTokenClient, OidcAdminClient oidcAdminClient,
                       SyncEntityRepository syncEntityRepository, SyncHistoryRepository syncHistoryRepository) {
        this.epagesClient = epagesClient;
        this.oidcTokenClient = oidcTokenClient;
        this.oidcAdminClient = oidcAdminClient;
        this.syncEntityRepository = syncEntityRepository;
        this.syncHistoryRepository = syncHistoryRepository;
    }


    public void syncOrdersAndCustomers() {

        /*
         * Load the last loading timestamp to filter orders that have been delivered since last load.
         */
        List<Item> filteredItems = getFilteredItems();

        /*
         * Get the customers from the orders and check if they need to be added to the authorization server.
         */
        checkForOrCreateUser(filteredItems);

        // TODO OHO Update warehouse stocks using quantities from new orders.

        /*
         * This current datetime is used to save it to the loading history. It will be
         * used for next orders load.
         */
        updateSyncHistory();
    }


    void updateSyncHistory() {
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(now, ZoneId.systemDefault());
        LocalDateTime utc = zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        SyncHistory syncHistory = new SyncHistory();
        Optional<SyncEntity> syncEntity = syncEntityRepository.findByName("Orders");
        if (syncEntity.isEmpty()) {
            String message = "No sync entity 'Orders' found.";
            log.error(message);
            throw new RecordNotFoundException(message);
        }
        syncHistory.setTimestamp(utc);
        syncHistoryRepository.save(syncHistory);
    }


    void checkForOrCreateUser(List<Item> filteredItems) {
        filteredItems.forEach(item -> {
            BillingAddress billingAddress = item.getBillingAddress();
            String emailAddress = billingAddress.getEmailAddress();
            String firstName = billingAddress.getFirstName();
            String lastName = billingAddress.getLastName();

            Token token = oidcTokenClient.token();
            User user = oidcAdminClient.getUserBy(emailAddress, token).orElseGet(() -> {
                String requiredAction = oidcAdminClient.requiredActionAsString(UserModel.RequiredAction.UPDATE_PASSWORD);
                UserInput userInput = createUserInput(emailAddress, firstName, lastName, requiredAction);
                oidcAdminClient.postUser(userInput, token);
                log.debug("userInput: {}:", userInput);
                OidcUserMapper oidcUserMapper = Mappers.getMapper(OidcUserMapper.class);
                return oidcUserMapper.toUser(userInput);
            });
            log.debug("user: {}", user);
        });
    }


    List<Item> getFilteredItems() {
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
    static UserInput createUserInput(String emailAddress, String firstName, String lastName, String requiredAction) {
        UserInput userInput = new UserInput();
        userInput.setUsername(emailAddress);
        userInput.setEmail(emailAddress);
        userInput.setFirstName(firstName);
        userInput.setLastName(lastName);
        userInput.setEmailVerified(Boolean.TRUE);
        userInput.setEnabled(Boolean.TRUE);
        userInput.setRequiredActions(List.of(requiredAction));
        return userInput;
    }

}
