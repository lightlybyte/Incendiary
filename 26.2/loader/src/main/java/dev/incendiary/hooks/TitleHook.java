package dev.incendiary.hooks;

import net.minecraft.client.Minecraft;
import dev.incendiary.api.Events;
import dev.incendiary.api.event.GameReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TitleHook {

    private static final Logger LOGGER = LoggerFactory.getLogger("Incendiary");
    private static boolean applied = false;

    public static void inject() {
        if (applied) return;
        applied = true;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            LOGGER.warn("Minecraft instance null during title hook");
            return;
        }
        var window = mc.getWindow();
        if (window == null) {
            LOGGER.warn("Window null during title hook");
            return;
        }
        window.setTitle("Minecraft 26.2 - Incendiary Mod Loader");
        LOGGER.info("title set");
    }
}