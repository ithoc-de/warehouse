package de.ithoc.warehouse.external.epages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile(value = "production")
public class EpagesTaskProd implements EpagesTask {

//    private final RestClient restClient;

/*    public EpagesTaskProd(RestClient restClient) {
        this.restClient = restClient;
    }
*/

    /*
    @Async
    @Scheduled(cron = "0 0/10 * * * ?")
    public Customers fetchCustomers() {

        Customers customers = restClient.get("/customers", Map.of(), Customers.class);
        log.debug("{}", customers);

        // TODO OHO Implement the mapping and storage of customers to clients.

        return null;
    }
    */

}
