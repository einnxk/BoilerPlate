package com.thencproject.papership.bind;

/**
 * An Exception while internal handling of the main class
 * and the registration of services
 * Makes it more easily to spot errors related to that
 * kind of problems
 */
public class InternalBootstrapException extends RuntimeException {

    /**
     * Basic Constructor because that exception can be used for alle kinds
     * of errors
     * @param message a message that is printed out before the stacktrace
     * @param cause a throwable cause that is printed out as the stacktrace
     *              cause
     */
    public InternalBootstrapException(String message, Throwable cause) {
        super(message, cause);
    }
}