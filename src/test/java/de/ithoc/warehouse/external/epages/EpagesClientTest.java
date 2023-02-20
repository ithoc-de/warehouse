package de.ithoc.warehouse.external.epages;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import static org.junit.jupiter.api.Assertions.*;

class EpagesClientTest {

    @Test
    public void mapEpagesDateToJava() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dateString = "2015-11-03T08:48:26Z";
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);

        assertEquals(2015, dateTime.getYear());
    }

    @Test
    public void mapJavaDateToEpages() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime localDateTime = LocalDateTime.of(2023, Month.FEBRUARY, 12, 21, 48, 36, 0);
        String strDateTime = formatter.format(localDateTime);

        assertEquals("2023-02-12T21:48:36.000Z", strDateTime);
    }


}