package dev.incendiary.api;

/**
 * Single entrypoint every Incendiary mod implements.
 */
public interface IncendiaryMod {
    /**
     * Called once after game-ready discovery, on the game thread.
     *
     * @param ctx initialization context for this mod
     */
    void onInitialize(ModContext ctx);
}
