package de.ithoc.warehouse.domain.synchronization;

public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException(String message) {
        super(message);
    }

}
