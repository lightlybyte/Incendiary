package dev.incendiary.hooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LifecycleHook {

    private static final Logger LOGGER = LoggerFactory.getLogger("Incendiary");
    private static boolean firstTickFired = false;

    public static void inject() {
        if (firstTickFired) return;
        firstTickFired = true;

        LOGGER.info("first client tick — Minecraft is alive");
        onGameReady();
    }

    private static void onGameReady() {
        LOGGER.info("game ready — mods would initialize here");
    }
}