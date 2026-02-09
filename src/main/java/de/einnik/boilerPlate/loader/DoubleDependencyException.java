package de.einnik.boilerPlate.loader;

public class DoubleDependencyException extends RuntimeException {

    public DoubleDependencyException(String message) {
        super(message);
    }
}