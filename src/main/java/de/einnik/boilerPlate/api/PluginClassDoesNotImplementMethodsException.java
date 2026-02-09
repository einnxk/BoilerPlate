package de.einnik.boilerPlate.api;

public class PluginClassDoesNotImplementMethodsException extends RuntimeException {

    public PluginClassDoesNotImplementMethodsException(Exception message) {
        super(message);
    }
}