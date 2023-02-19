package de.ithoc.warehouse.external.epages;

import de.ithoc.warehouse.external.RestClient;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OrdersLoader {

    private final RestClient restClient;

    public OrdersLoader(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Item> loadItems(LocalDateTime updatedFromUtcDateTime) {

        String formattedDateTime = formatDateTime(updatedFromUtcDateTime);
        Map<String, String> variables = Map.of(
                "updatedFrom", formattedDateTime,
                "deliveredOn", "true",
                "page", "1"
        );
        Orders orders = restClient.get("/orders", variables, Orders.class);
        log.debug("{}", orders);

        List<Item> items = new ArrayList<>(orders.getItems());

        /*
         * Ascertain the number of pages for pagination to flatten the items.
         * Also consider the order status and the duration, means only delivered orders
         * from last order load are filtered and saved to this warehouse.
         */
        long pages = numberOfPages(orders.getResults(), orders.getResultsPerPage());
        for (int pageCount = 2; pageCount <= pages; pageCount++) {
            Map<String, String> queryParameters = Map.of(
                    "updatedFrom", formattedDateTime,
                    "deliveredOn", "true",
                    "page", "" + pageCount
            );
            Orders nextOrders = restClient.get("/orders", queryParameters, Orders.class);
            items.addAll(nextOrders.getItems());
        }

        log.debug("items: {}", items);
        return items;
    }

    private String formatDateTime(LocalDateTime localDateTime) {
        String formattedDateTime = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").format(localDateTime) + "Z";
        log.debug("formattedDateTime: {}", formattedDateTime);

        return formattedDateTime;
    }

    private long numberOfPages(long results, long resultsPerPage) {
        // This division returns an integer rounded up.
        double numberOfPages = ((double) results) / resultsPerPage;
        long ceiledNumberOfPages = (long) Math.ceil(numberOfPages);
        log.debug("ceiledNumberOfPages: {}", ceiledNumberOfPages);

        return ceiledNumberOfPages;
    }

}
