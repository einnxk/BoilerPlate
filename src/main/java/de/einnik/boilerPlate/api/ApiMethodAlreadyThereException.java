package de.einnik.boilerPlate.api;

public class ApiMethodAlreadyThereException extends RuntimeException {

    public ApiMethodAlreadyThereException(String message) {
        super(message);
    }
}