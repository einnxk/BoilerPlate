package de.einnik.boilerPlate.bind;

public class AutoRegistrationException extends RuntimeException {

    public AutoRegistrationException(String message) {
        super(message);
    }

    public AutoRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}