package de.ithoc.warehouse.external.epages;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpagesTaskTest {

    @Test
    public void cetToUtc() {
        LocalDateTime localDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 13, 6, 6, 6, 124);

        EpagesTaskDev epagesTaskDev = new EpagesTaskDev(null, null);
        LocalDateTime utcDateTime = epagesTaskDev.cetToUtc(localDateTime);

        assertEquals(5, utcDateTime.getHour());
        assertEquals(6, utcDateTime.getMinute());
    }

    @Test
    public void utcToCet() {
        LocalDateTime localDateTime = LocalDateTime.of(
                2023, Month.FEBRUARY, 13, 6, 6, 6, 124);

        EpagesTaskDev epagesTaskDev = new EpagesTaskDev(null, null);
        LocalDateTime cetDateTime = epagesTaskDev.utcToCet(localDateTime);

        assertEquals(7, cetDateTime.getHour());
        assertEquals(6, cetDateTime.getMinute());
    }

    @Test
    public void numberOfPages8By10() {
        int results = 8;
        int resultsPerPage = 10;
        EpagesTaskDev epagesTaskDev = new EpagesTaskDev(null, null);

        long pages = epagesTaskDev.numberOfPages(results, resultsPerPage);

        assertEquals(1, pages);
    }

    @Test
    public void numberOfPages98By10() {
        int results = 98;
        int resultsPerPage = 10;
        EpagesTaskDev epagesTaskDev = new EpagesTaskDev(null, null);

        long pages = epagesTaskDev.numberOfPages(results, resultsPerPage);

        assertEquals(10, pages);
    }

}
