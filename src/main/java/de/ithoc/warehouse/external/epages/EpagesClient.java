package de.ithoc.warehouse.external.epages;

import de.ithoc.warehouse.external.authprovider.schema.users.User;
import de.ithoc.warehouse.external.epages.schema.customers.Customers;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EpagesClient {

    private final WebClient webClient;
    private final String apiUrl;
    private final String apiKey;


    public EpagesClient(
            WebClient webClient,
            @Value("${epages.api.url}") String apiUrl,
            @Value("${epages.api.key}") String apiKey
    ) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.webClient = webClient;
    }


    public List<Item> orders(LocalDateTime fromDeliveredOnDateUtc) {

        String fromDateTimeUtc = toOrderDeliveredOnDate(fromDeliveredOnDateUtc);
        Orders orders = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(apiUrl + "/orders")
                        .queryParam("updatedFrom", fromDateTimeUtc)
                        .queryParam("deliveredOn", "true")
                        .queryParam("page", "1")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Orders.class)
                .block();
        log.debug("{}", orders);

        if(orders == null) {
            // if no order items are available we are done
            return List.of();
        }

        /*
         * Ascertain the number of pages for pagination to flatten the items.
         * Also consider the order status and the duration, means only delivered orders
         * from last order load are filtered and saved to this warehouse.
         */
        List<Item> items = new ArrayList<>(orders.getItems());
        long pages = numberOfPages(orders.getResults(), orders.getResultsPerPage());
        for (int pageCount = 2; pageCount <= pages; pageCount++) {
            final int finalPageCount = pageCount;
            Orders furtherOrders = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path(apiUrl + "/orders")
                            .queryParam("updatedFrom", fromDateTimeUtc)
                            .queryParam("deliveredOn", "true")
                            .queryParam("page", finalPageCount)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(Orders.class)
                    .block();
            if(furtherOrders != null) {
                items.addAll(furtherOrders.getItems());
            }
        }
        log.debug("items: {}", items);

        return items;
    }


    public Customers customers() {

        return webClient.get()
                .uri(apiUrl + "/customers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Customers.class)
                .block();
    }


    private String toOrderDeliveredOnDate(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return formatter.format(localDateTime);
    }


    private long numberOfPages(long results, long resultsPerPage) {
        // This division returns an integer rounded up.
        double numberOfPages = ((double) results) / resultsPerPage;
        long ceiledNumberOfPages = (long) Math.ceil(numberOfPages);
        log.debug("ceiledNumberOfPages: {}", ceiledNumberOfPages);

        return ceiledNumberOfPages;
    }


}
