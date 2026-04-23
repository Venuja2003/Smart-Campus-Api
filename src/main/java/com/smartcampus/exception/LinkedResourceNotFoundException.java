package com.smartcampus.exception;

/**
 * Thrown when a POST Sensor references a roomId that does not exist.
 * Mapped to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super("Linked resource '" + resourceType + "' with ID '" + resourceId + "' was not found.");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
