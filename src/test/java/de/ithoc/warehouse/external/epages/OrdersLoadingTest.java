package de.ithoc.warehouse.external.epages;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.RestClient;
import de.ithoc.warehouse.external.schema.orders.Orders;
import de.ithoc.warehouse.persistence.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdersLoadingTest {

    @Mock
    private LoadingHistoryRepository loadingHistoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private OrdersLoading ordersLoading;

    @Test
    public void loadOrdersOnePage() throws IOException {
        Orders orders = loadTestOrders();
        LocalDateTime deliverDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 14,
                8, 10, 0, 0);
        LocalDateTime deliverUtc = ordersLoading.cetToUtc(deliverDateTime);
        String dateTime = ordersLoading.formatDateTime(deliverUtc);
        orders.getItems().forEach(item -> item.setDeliveredOn(dateTime));
        when(restClient.get(eq("/orders"), eq(Map.of("page", "1")),
                ArgumentMatchers.<Class<Orders>>any()))
                .thenReturn(orders);

        Optional<LoadingHistory> lastLoadingHistory = Optional.of(new LoadingHistory());
        String lastLoadDateTime = ordersLoading.formatDateTime(deliverUtc.minusDays(3));
        lastLoadingHistory.get().setTimestamp(lastLoadDateTime);
        when(loadingHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(lastLoadingHistory);

        String customerNumber1 = "1001";
        String customerId1 = "632BA7E6-2F21-C482-1565-0A0C05B41001";
        String orderId11 = "632BA7E6-944C-0EC9-D49C-0A0C05B41101";
        Optional<Customer> customer1 = Optional.of(createCustomer(
                customerNumber1, customerId1,
                "Mark", "Knopfler", "mark.knopfler@example.com"));
        when(customerRepository.findByCustomerNumber(customerNumber1)).thenReturn(customer1);

        String customerNumber2 = "1002";
        String customerId2 = "632BA7E6-2F21-C482-1565-0A0C05B41002";
        String orderId21 = "632BA7E6-944C-0EC9-D49C-0A0C05B41102";
        String orderId22 = "632BA7E6-944C-0EC9-D49C-0A0C05B41103";
        String orderId23 = "632BA7E6-944C-0EC9-D49C-0A0C05B41104";
        Optional<Customer> customer2 = Optional.of(createCustomer(
                customerNumber2, customerId2,
                "Bruce", "Springsteen", "bruce.springsteen@example.com"));
        when(customerRepository.findByCustomerNumber(customerNumber2)).thenReturn(customer2);

        String customerNumber3 = "1005";
        String customerId3 = "632BA7E6-2F21-C482-1565-0A0C05B41005";
        String orderId31 = "632BA7E6-944C-0EC9-D49C-0A0C05B41105";
        Optional<Customer> customer3 = Optional.of(createCustomer(
                customerNumber3, customerId3,
                "Tracy", "Chapman", "tracy.chapman@example.com"));
        when(customerRepository.findByCustomerNumber(customerNumber3)).thenReturn(customer3);

        ordersLoading.loadOrders();

        verify(customerRepository, times(5)).findByCustomerNumber(anyString());
        Assertions.assertTrue(true);
    }

    private Customer createCustomer(String customerNumber, String customerId, String firstName, String lastName, String emailAddress) {
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        customer.setCustomerId(customerId);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmailAddress(emailAddress);
        return customer;
    }

    @Test
    public void cetToUtc() {
        LocalDateTime localDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 13, 6, 6, 6, 124);

        OrdersLoading ordersLoading = new OrdersLoading(null, null, null, null);
        LocalDateTime utcDateTime = ordersLoading.cetToUtc(localDateTime);

        assertEquals(5, utcDateTime.getHour());
        assertEquals(6, utcDateTime.getMinute());
    }

    @Test
    public void utcToCet() {
        LocalDateTime localDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 13, 6, 6, 6, 124);

        OrdersLoading ordersLoading = new OrdersLoading(null, null, null, null);
        LocalDateTime cetDateTime = ordersLoading.utcToCet(localDateTime);

        assertEquals(7, cetDateTime.getHour());
        assertEquals(6, cetDateTime.getMinute());
    }

    @Test
    public void numberOfPages8By10() {
        int results = 8;
        int resultsPerPage = 10;
        OrdersLoading ordersLoading = new OrdersLoading(null, null, null, null);

        long pages = ordersLoading.numberOfPages(results, resultsPerPage);

        assertEquals(1, pages);
    }

    @Test
    public void numberOfPages20By10() {
        int results = 20;
        int resultsPerPage = 10;
        OrdersLoading ordersLoading = new OrdersLoading(null, null, null, null);

        long pages = ordersLoading.numberOfPages(results, resultsPerPage);

        assertEquals(2, pages);
    }

    @Test
    public void numberOfPages98By10() {
        int results = 98;
        int resultsPerPage = 10;
        OrdersLoading ordersLoading = new OrdersLoading(null, null, null, null);

        long pages = ordersLoading.numberOfPages(results, resultsPerPage);

        assertEquals(10, pages);
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
