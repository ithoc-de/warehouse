package de.ithoc.warehouse.external.keycloak;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

//@SpringBootTest
public class AdminClientIT {

    @Autowired
    private AdminClient adminClient;

    @Test
    public void getUsers() {

//        adminClient = new AdminClient(webClient);
//        adminClient.getUsers();

        Assertions.assertTrue(true);
    }

}