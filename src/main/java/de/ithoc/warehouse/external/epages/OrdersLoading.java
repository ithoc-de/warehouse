package de.ithoc.warehouse.external.epages;

import de.ithoc.warehouse.external.RestClient;
import de.ithoc.warehouse.external.schema.orders.Item;
import de.ithoc.warehouse.external.schema.orders.Orders;
import de.ithoc.warehouse.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@Profile(value = "!production")
public class OrdersLoading implements EpagesScheduler {

    private final Shop shop;
    private final LoadingHistoryRepository loadingHistoryRepository;
    private final CustomerRepository customerRepository;
    private final RestClient restClient;

    public OrdersLoading(Shop shop, LoadingHistoryRepository loadingHistoryRepository, CustomerRepository customerRepository, RestClient restClient) {
        this.shop = shop;
        this.loadingHistoryRepository = loadingHistoryRepository;
        this.customerRepository = customerRepository;
        this.restClient = restClient;
    }

    private void saveLoadingHistory(String dateTime) {
        LoadingHistory loadingHistory = new LoadingHistory();
        loadingHistory.setTimestamp(dateTime);
        loadingHistory.setShop(shop);
        loadingHistoryRepository.save(loadingHistory);
    }

    String formatDateTime(LocalDateTime localDateTime) {
        String formattedDateTime = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").format(localDateTime) + "Z";
        log.debug("formattedDateTime: {}", formattedDateTime);

        return formattedDateTime;
    }

    @Scheduled(fixedDelay = 1000)
    public void loadOrders() {
        log.trace(new Exception().getStackTrace()[0].getMethodName());

        /*
         * Load the last loading timestamp to filter orders that have been delivered since last load.
         */
        Optional<LoadingHistory> lastLoadingTimestamp = loadingHistoryRepository.findTopByOrderByTimestampDesc();
        /*
         * This current datetime is used to save it to the loading history. It will be
         * used for next orders load.
         */
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime utc = cetToUtc(now);
        String dateTime = formatDateTime(utc);
        saveLoadingHistory(dateTime);

        /*
         * Load orders by calling the ePages API.
         */
        Orders orders = restClient.get("/orders", Map.of("page", "1"), Orders.class);
        log.debug("{}", orders);

        /*
         * Ascertain the number of pages for pagination to flatten the items.
         * Also consider the order status and the duration, means only delivered orders
         * from last order load are filtered and saved to this warehouse.
         */
        List<Item> items = new ArrayList<>(orders.getItems());
        long pages = numberOfPages(orders.getResults(), orders.getResultsPerPage());
        for (int pageCount = 2; pageCount <= pages; pageCount++) {
            Map<String, String> variables = Map.of("page", "pageCount");
            Orders nextOrders = restClient.get("/orders", variables, Orders.class);
            items.addAll(nextOrders.getItems());
        }

        /*
         * Check if there are new orders since last load and create customers for them or
         * adjust the quantity in warehouse respectively.
         */
        List<Item> filteredItems = items.stream()
                .filter(item -> item.getDeliveredOn() != null)
                .filter(item -> (lastLoadingTimestamp.orElseGet(() -> {
                    LoadingHistory loadingHistory = new LoadingHistory();
                    loadingHistory.setTimestamp("1970-01-01T00:00:00.000Z");
                    return loadingHistory;
                }).getTimestamp().compareTo(item.getDeliveredOn()) <= 0))
                .toList();

        filteredItems.forEach(item -> {
                    String customerNumber = item.getCustomerNumber();
                    customerRepository.findByCustomerNumber(customerNumber).orElseGet(() -> {
                        String resource = "/customers/" + item.getCustomerId();
                        de.ithoc.warehouse.external.schema.customers.customer.Customer epagesCustomer = restClient.get(
                                resource, Map.of(),
                                de.ithoc.warehouse.external.schema.customers.customer.Customer.class);
                        Customer newCustomer = new Customer();
                        newCustomer.setCustomerId(epagesCustomer.getCustomerId());
                        newCustomer.setCustomerNumber(epagesCustomer.getCustomerNumber());
                        newCustomer.setFirstName(epagesCustomer.getBillingAddress().getFirstName());
                        newCustomer.setLastName(epagesCustomer.getBillingAddress().getLastName());
                        newCustomer.setEmailAddress(epagesCustomer.getBillingAddress().getEmailAddress());
                        customerRepository.save(newCustomer);
                        return newCustomer;
                    });
                });
    }

    public LocalDateTime cetToUtc(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(localDateTime, ZoneId.of("CET"));
        return zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public LocalDateTime utcToCet(LocalDateTime localDateTime) {
        ZonedDateTime utcTimeZonedUtc = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
        return utcTimeZonedUtc.withZoneSameInstant(ZoneId.of("CET")).toLocalDateTime();
    }

    public long numberOfPages(long results, long resultsPerPage) {
        // This division returns an integer rounded up.
        double result = ((double) results) / resultsPerPage;
        return (long) Math.ceil(result);
    }

}
