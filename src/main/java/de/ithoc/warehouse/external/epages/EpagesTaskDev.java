package de.ithoc.warehouse.external.epages;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ithoc.warehouse.external.schema.customers.Customers;
import de.ithoc.warehouse.external.schema.orders.Orders;
import de.ithoc.warehouse.persistence.ClientRepository;
import de.ithoc.warehouse.persistence.LoadingHistory;
import de.ithoc.warehouse.persistence.LoadingHistoryRepository;
import de.ithoc.warehouse.persistence.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@Profile(value = "!production")
public class EpagesTaskDev implements EpagesTask {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Shop shop;
    private final LoadingHistoryRepository loadingHistoryRepository;

    public EpagesTaskDev(Shop shop, LoadingHistoryRepository loadingHistoryRepository) {
        this.shop = shop;
        this.loadingHistoryRepository = loadingHistoryRepository;
    }

    public String loadLastFetchTimestamp() {
//        Fetch lastFetch = fetchRepository.findTopByOrderByLastFetchDesc();

  //      return lastFetch.getLastFetch();
        return null;
    }

    public void saveLoadingHistory(LocalDateTime localDateTime) {
        LocalDateTime utcDateTime = cetToUtc(localDateTime);
        String utcFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").format(utcDateTime);
        utcFormatted += "Z";

        LoadingHistory loadingHistory = new LoadingHistory();
        loadingHistory.setTimestamp(utcFormatted);
        loadingHistory.setShop(shop);
        loadingHistoryRepository.save(loadingHistory);
    }

    @Scheduled(fixedDelay = 1000)
    public Orders loadOrders() {
        log.trace("loadOrders");

        saveLoadingHistory(LocalDateTime.now());

        return null;
    }

    //@Scheduled(cron = "0 0/10 * * * ?")
    public Customers fetchCustomers(String lastFetchTimestamp) throws IOException {

        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("epages/schema/customers/customers.json");
        assert inputStream != null;
        String json =  new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        Customers customers = objectMapper.readValue(json, Customers.class);

        log.debug("{}", customers);

        return customers;
    }


    public LocalDateTime cetToUtc(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTimeCet = ZonedDateTime.of(localDateTime, ZoneId.of("CET"));
        return zonedDateTimeCet.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public LocalDateTime utcToCet(LocalDateTime localDateTime) {
        ZonedDateTime utcTimeZonedUtc = ZonedDateTime.of(localDateTime,ZoneId.of("UTC"));
        return utcTimeZonedUtc.withZoneSameInstant(ZoneId.of("CET")).toLocalDateTime();
    }

    public long numberOfPages(long results, long resultsPerPage) {
        // This division returns an integer rounded up.
        return (results / resultsPerPage) + 1;
    }

}
