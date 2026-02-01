package com.example.resumescreener.exception;

/**
 * Custom exception for resource not found scenarios.
 * Used when a requested resource (e.g., Job, Resume) does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
