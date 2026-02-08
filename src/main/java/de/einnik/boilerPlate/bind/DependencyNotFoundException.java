package de.einnik.boilerPlate.bind;

public class DependencyNotFoundException extends RuntimeException {

    public DependencyNotFoundException(String message) {
        super(message);
    }
}