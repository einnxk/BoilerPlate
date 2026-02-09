package de.einnik.boilerPlate.loader;

public class FileCopyException extends RuntimeException {

    public FileCopyException(String message) {
        super(message);
    }

    public FileCopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileCopyException(Throwable cause) {
        super(cause);
    }
}