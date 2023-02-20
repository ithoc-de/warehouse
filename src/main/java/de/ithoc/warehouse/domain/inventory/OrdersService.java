package de.ithoc.warehouse.domain.inventory;

import de.ithoc.warehouse.external.epages.OrdersLoader;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrdersService {

    private final OrdersLoader ordersLoader;

    private final LoadingHistoryRepository loadingHistoryRepository;

    public OrdersService(OrdersLoader ordersLoader, LoadingHistoryRepository loadingHistoryRepository) {
        this.ordersLoader = ordersLoader;
        this.loadingHistoryRepository = loadingHistoryRepository;
    }

    private void saveLoadingHistory(LocalDateTime dateTime) {
        LoadingHistory loadingHistory = new LoadingHistory();
        loadingHistory.setTimestamp(dateTime);
// TODO OHO        loadingHistory.setShop(shop);
        loadingHistoryRepository.save(loadingHistory);
    }

    String formatDateTime(LocalDateTime localDateTime) {
        String formattedDateTime = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").format(localDateTime) + "Z";
        log.debug("formattedDateTime: {}", formattedDateTime);

        return formattedDateTime;
    }

// TODO OHO Enable scheduler    @Scheduled(fixedDelay = (24 * 60 * 1000))
    public void updateInventory() {
        log.trace(new Exception().getStackTrace()[0].getMethodName());

        /*
         * This current datetime is used to save it to the loading history. It will be
         * used for next orders load.
         */
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(now, ZoneId.systemDefault());
        LocalDateTime utc = zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        saveLoadingHistory(utc);

        /*
         * Load the last loading timestamp to filter orders that have been delivered since last load.
         */
        Optional<LoadingHistory> lastLoadingTimestamp = loadingHistoryRepository.findTopByOrderByTimestampDesc();
        LocalDateTime updateFromUtcDateTime = lastLoadingTimestamp.orElseGet(() -> {
            LoadingHistory loadingHistory = new LoadingHistory();
            loadingHistory.setTimestamp(LocalDateTime.of(
                    1970, Month.JANUARY.getValue(), 1,
                    0, 0, 0, 0));
            return loadingHistory;
        }).getTimestamp();

        /*
         * Actually get the orders from last check date and time from ePages.
         */
        List<Item> filteredItems = ordersLoader.loadItems(updateFromUtcDateTime);
        log.debug("filteredItems: {}", filteredItems);

        /*
         * Synchronize ePage customers and this warehouse.
         */
        /* TODO OHO Remove this snippet when verified not needed anymore
        filteredItems.forEach(item -> {
            String customerNumber = item.getCustomerNumber();
            customerRepository.findByCustomerNumber(customerNumber).orElseGet(() -> {
                Customer newCustomer = new Customer();
                newCustomer.setCustomerId(item.getCustomerId());
                newCustomer.setCustomerNumber(item.getCustomerNumber());
                newCustomer.setFirstName(item.getBillingAddress().getFirstName());
                newCustomer.setLastName(item.getBillingAddress().getLastName());
                newCustomer.setEmailAddress(item.getBillingAddress().getEmailAddress());
                customerRepository.save(newCustomer);
                return newCustomer;
            });
        });
         */
    }

}