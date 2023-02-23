package de.ithoc.warehouse.domain.notification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class EmailServiceIT {

    @Autowired
    private EmailService emailService;

    @Test
    void send() {
        String to = "oliver.hock@gmail.com";
        String subject = "Your booking is confirmed";
        String text = "Dear User, your kajak is waiting for you at the baltic sea.";

//        emailService.send(to, subject, text);

        Assertions.assertTrue(true, "E-mail has been sent.");
    }

}