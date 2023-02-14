package de.ithoc.warehouse.domain.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.epages.OrdersLoader;
import de.ithoc.warehouse.external.schema.orders.Orders;
import de.ithoc.warehouse.persistence.Customer;
import de.ithoc.warehouse.persistence.CustomerRepository;
import de.ithoc.warehouse.persistence.LoadingHistory;
import de.ithoc.warehouse.persistence.LoadingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdersServiceTest {

    @Mock
    private OrdersLoader ordersLoader;

    @Mock
    private LoadingHistoryRepository loadingHistoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrdersService ordersService;


    @Test
    public void loadOrdersOnePage() throws IOException {
        Orders orders = loadTestOrders();
        LocalDateTime deliverDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 14,
                8, 10, 0, 0);
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(deliverDateTime, ZoneId.systemDefault());
        LocalDateTime deliverUtc = zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        String dateTime = ordersService.formatDateTime(deliverUtc);
        orders.getItems().forEach(item -> item.setDeliveredOn(dateTime));

        Optional<LoadingHistory> lastLoadingHistory = Optional.of(new LoadingHistory());
        lastLoadingHistory.get().setTimestamp(deliverUtc.minusDays(3));
        when(loadingHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(lastLoadingHistory);

        when(ordersLoader.loadItems(any(LocalDateTime.class))).thenReturn(orders.getItems());

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
        Optional<Customer> customer3 = Optional.empty();
        when(customerRepository.findByCustomerNumber(customerNumber3)).thenReturn(customer3);

        ordersService.updateInventory();

        verify(loadingHistoryRepository, times(1)).save(any(LoadingHistory.class));
        verify(loadingHistoryRepository, times(1)).findTopByOrderByTimestampDesc();
        verify(customerRepository, times(5)).findByCustomerNumber(anyString());
        verify(customerRepository, times(1)).save(any(Customer.class));
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

    private Orders loadTestOrders() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("test-orders.json");
        assert inputStream != null;
        String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Orders.class);
    }

}
