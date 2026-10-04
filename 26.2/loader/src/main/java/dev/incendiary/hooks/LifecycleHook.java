package dev.incendiary.hooks;

import dev.incendiary.api.Events;
import dev.incendiary.api.event.ClientTickEvent;
import dev.incendiary.api.event.GameReadyEvent;
import dev.incendiary.transform.Trace;

public final class LifecycleHook {

    private static boolean readyFired = false;
    private static int tickCounter = 0;

    public static void inject() {
        tickCounter++;

        Trace.log("[Incendiary] LifecycleHook.inject() tick #" + tickCounter);
        Events.CLIENT_TICK.fire(ClientTickEvent.INSTANCE);

        if (!readyFired) {
            readyFired = true;
            Trace.log("[Incendiary] GAME_READY firing");
            Events.GAME_READY.fire(GameReadyEvent.INSTANCE);
        }
    }
}