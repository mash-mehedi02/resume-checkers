package com.example.resumescreener.exception;

/**
 * Custom exception for text extraction/parsing failures.
 * Used when Apache Tika fails to extract text from document.
 */
public class ParsingException extends RuntimeException {

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
