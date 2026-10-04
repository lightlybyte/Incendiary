package dev.incendiary.api;

import java.nio.file.Path;
import org.slf4j.Logger;

/**
 * Initialization context handed to {@link IncendiaryMod#onInitialize(ModContext)}.
 * Deliberately exposes no Minecraft types so the API stays version-stable.
 */
public interface ModContext {
    /**
     * @return the mod id declared in incendiary.mod.json
     */
    String modId();

    /**
     * @return SLF4J logger scoped to this mod
     */
    Logger logger();

    /**
     * @return the runtime side the game is running on
     */
    Side side();

    /**
     * Creates {@code <instanceRoot>/config/<modId>/} on first call if absent.
     *
     * @return the per-mod config directory
     */
    Path configDir();
}
