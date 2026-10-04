package dev.incendiary.api.exceptions;

/**
 * Thrown for malformed mod metadata. Reserved for future strict parsing.
 */
public class ModMetadataException extends RuntimeException {
    public ModMetadataException(String message) {
        super(message);
    }

    public ModMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
