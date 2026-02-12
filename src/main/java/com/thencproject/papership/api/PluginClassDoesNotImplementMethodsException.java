package com.thencproject.papership.api;

public class PluginClassDoesNotImplementMethodsException extends RuntimeException {

    public PluginClassDoesNotImplementMethodsException(Exception message) {
        super(message);
    }
}