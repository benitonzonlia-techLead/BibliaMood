package com.bnz.bibliamood.exception;

/**
 * Custom exception for BibliaMood application.
 */
public class BibliaMoodException extends RuntimeException {

    public BibliaMoodException(String message) {
        super(message);
    }

    public BibliaMoodException(String message, Throwable cause) {
        super(message, cause);
    }
}