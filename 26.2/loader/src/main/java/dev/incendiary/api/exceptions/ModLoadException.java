package dev.incendiary.api.exceptions;

/**
 * Thrown when a mod entrypoint cannot be loaded or initialized. Reserved for prompt 4.
 */
public class ModLoadException extends RuntimeException {
    public ModLoadException(String message) {
        super(message);
    }

    public ModLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
