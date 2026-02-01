package com.example.resumescreener.exception;

/**
 * Custom exception for invalid file uploads.
 * Used when file type is not supported or file is corrupted.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
