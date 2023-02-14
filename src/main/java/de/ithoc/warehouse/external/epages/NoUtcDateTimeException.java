package de.ithoc.warehouse.external.epages;

import java.time.ZonedDateTime;

public class NoUtcDateTimeException extends RuntimeException {

    public NoUtcDateTimeException(ZonedDateTime zonedDateTime) {
        super("Datetime is not UTC: " + zonedDateTime);
    }

}
