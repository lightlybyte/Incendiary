package dev.incendiary.api;

import dev.incendiary.transform.Environment;
import java.util.List;
import java.util.Optional;

/**
 * Version handshake and runtime info for mods.
 */
public final class IncendiaryApi {

    /** Loader version mods declare compatibility against. */
    public static final String VERSION = "1.0.0";

    /** Minecraft version this loader release targets. */
    public static final String MC_TARGET = "26.2";

    private IncendiaryApi() {}

    /**
     * Reserved: returns an unmodifiable empty list until v1.1.
     */
    public static List<ModContainer> loadedMods() {
        return List.of();
    }

    /**
     * Reserved: returns false until v1.1.
     */
    public static boolean isModLoaded(String id) {
        return false;
    }

    /**
     * Reserved: returns empty until v1.1.
     */
    public static Optional<ModContainer> getMod(String id) {
        return Optional.empty();
    }

    public static String loaderName() {
        return "Incendiary";
    }

    public static String loaderVersion() {
        return VERSION;
    }

    /**
     * @return the side the game is currently running on
     */
    public static Side side() {
        return Environment.current();
    }
}
