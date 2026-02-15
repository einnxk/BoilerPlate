package com.thencproject.papership.bind;

/**
 * An Exception while trying to fetch the dependency plugins
 * that are defined in the plugin configuration file annotation
 * Makes it more easily to spot errors related to that
 * kind of problems
 */
public class DependencyNotFoundException extends RuntimeException {

    /**
     * Basic Constructor - normally the missing dependency
     * should be named here
     * @param message a message with the exception type
     */
    public DependencyNotFoundException(String message) {
        super(message);
    }
}