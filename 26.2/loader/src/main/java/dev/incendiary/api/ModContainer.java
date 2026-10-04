package dev.incendiary.api;

import java.nio.file.Path;

/**
 * A discovered mod. Reserved for future use by IncendiaryApi.loadedMods().
 */
public interface ModContainer {
    String id();
    String name();
    String version();
    Side side();
    Path jarPath();
    boolean isLoaded();
}
