package de.ithoc.warehouse.domain.synchronization;

public class MultipleOAuth2UsersExeption extends RuntimeException {

    public MultipleOAuth2UsersExeption(String message) {
        super(message);
    }

}
