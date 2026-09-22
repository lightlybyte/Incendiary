package dev.incendiary.hooks;

import dev.incendiary.api.Events;
import dev.incendiary.api.event.ClientTickEvent;
import dev.incendiary.api.event.GameReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LifecycleHook {

    private static final Logger LOGGER = LoggerFactory.getLogger("Incendiary");
    private static boolean readyFired = false;

    /**
     * Called by the transformed Minecraft.tick() at HEAD, on every tick.
     */
    public static void inject() {
        // Fire the per-tick event first so mods see the tick even on the
        // very first one.
        Events.CLIENT_TICK.fire(ClientTickEvent.INSTANCE);

        if (!readyFired) {
            readyFired = true;
            LOGGER.info("game ready");
            Events.GAME_READY.fire(GameReadyEvent.INSTANCE);
        }
    }
}