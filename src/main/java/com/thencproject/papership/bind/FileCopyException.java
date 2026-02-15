package com.thencproject.papership.bind;

/**
 * An Exception while copying the fields marked with the
 * annotations for auto field providing
 * Makes it more easily to spot errors related to that
 * kind of problems
 */
public class FileCopyException extends RuntimeException {

    /**
     * Basic constructor
     * @param cause an exception that is thrown
     */
    public FileCopyException(Throwable cause) {
        super(cause);
    }
}