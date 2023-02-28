package de.ithoc.warehouse.external.epages;

import de.ithoc.warehouse.external.epages.schema.customers.Customers;
import de.ithoc.warehouse.external.epages.schema.orders.Item;
import de.ithoc.warehouse.external.epages.schema.orders.Orders;
import de.ithoc.warehouse.external.epages.schema.orders.order.Order;
import de.ithoc.warehouse.external.epages.schema.products.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EpagesClient {

    private final WebClient webClient;
    private final URI apiUri;
    private final String apiKey;

    public EpagesClient(
            WebClient webClient,
            @Value("${epages.api.url}") String apiUri,
            @Value("${epages.api.key}") String apiKey
    ) {
        this.apiUri = URI.create(apiUri);
        this.apiKey = apiKey;
        this.webClient = webClient;
    }


    public Orders orders(LocalDateTime fromDateTimeUtc, long page) {

        String dateTimeStr = toOrderDeliveredOnDate(fromDateTimeUtc);
        Orders orders = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(apiUri.getScheme())
                        .host(apiUri.getHost())
                        .port(apiUri.getPort())
                        .path(apiUri.getPath() + "/orders")
                        .queryParam("updatedFrom", dateTimeStr)
                        .queryParam("deliveredOn", "true")
                        .queryParam("page", page)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Orders.class)
                .block();
        log.debug("orders: {}", orders);

        return orders;
    }


    /**
     * Load one single order by its id from the shop.
     *
     * @param orderId Order ID of the shop
     * @return Order
     */
    public Order order(String orderId) {

        Order order = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(apiUri.getScheme())
                        .host(apiUri.getHost())
                        .port(apiUri.getPort())
                        .path(apiUri.getPath() + "/orders/" + orderId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Order.class)
                .block();
        log.debug("order: {}", order);

        return order;
    }


    /**
     * Load and flatten order items by getting new orders since last synchronisation
     * and put the underlying items into a flat list.
     *
     * @param fromDeliveredOnDateUtc Last synchronisation timestamp in UTC
     * @return Flat list of order items
     */
    public List<Item> orderItems(LocalDateTime fromDeliveredOnDateUtc) {

        /*
         * Get the orders which contain a billing address holding customer details
         * like their names and e-mail addresses.
         */
        Orders orders = orders(fromDeliveredOnDateUtc, 1);
        log.debug("{}", orders);

        if(orders == null || orders.getResults() == null) {
            // if no order items are available we are done
            return List.of();
        }

        /*
         * Ascertain the number of pages for pagination to flatten the items.
         * Also consider the order status and the duration, means only delivered orders
         * from last order load are filtered and saved to this warehouse.
         */
        List<Item> items = flattenOrderItems(fromDeliveredOnDateUtc, orders);
        log.debug("items: {}", items);

        return items;
    }


    private List<Item> flattenOrderItems(LocalDateTime fromDateTimeUtc, Orders orders) {
        List<Item> items = new ArrayList<>(orders.getItems());
        long pages = numberOfPages(orders.getResults(), orders.getResultsPerPage());
        for (int pageCount = 2; pageCount <= pages; pageCount++) {
            Orders furtherOrders = orders(fromDateTimeUtc, pageCount);
             if(furtherOrders != null) {
                items.addAll(furtherOrders.getItems());
            }
        }
        log.debug("items: {}", items);

        return items;
    }


    public Customers getCustomers() {

        Customers customers = webClient.get()
                .uri(apiUri + "/customers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Customers.class)
                .block();
        log.debug("customers: {}", customers);

        return customers;
    }


    public Product product(String productId) {
        Product product = webClient.get()
                .uri(apiUri + "/products/" + productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(Product.class)
                .block();
        log.debug("product: {}", product);

        return product;
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
