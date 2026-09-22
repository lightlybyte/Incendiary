package dev.incendiary.agent;

import java.lang.instrument.Instrumentation;
import dev.incendiary.api.Events;
import dev.incendiary.api.Side;
import dev.incendiary.api.event.ClientTickEvent;
import dev.incendiary.api.event.GameReadyEvent;
import dev.incendiary.transform.Environment;
import dev.incendiary.transform.HookRegistry;
import dev.incendiary.transform.Transformer;

public final class IncendiaryAgent {

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[Incendiary] agent attached");
        System.out.println("[Incendiary] side: " + Environment.current());

        // --- Hooks ---
        // Register all hooks BEFORE installing the transformer, so that
        // when Minecraft classes start loading, the registry is populated.
        HookRegistry.register(
            "net/minecraft/client/Minecraft",
            "tick",
            "()V",
            "dev/incendiary/hooks/LifecycleHook",
            Side.CLIENT
        );

        // --- Temporary test listeners (remove once mod loading works) ---
        registerTestListeners();

        // --- Transformer ---
        inst.addTransformer(new Transformer(), false);
        System.out.println("[Incendiary] transformer installed");
    }

    /**
     * Temporary listeners to prove the event bus works end-to-end.
     * Delete this method once the test mod can register its own listeners.
     */
    private static void registerTestListeners() {
        final int[] tickCount = { 0 };

        Events.CLIENT_TICK.register(e -> {
            tickCount[0]++;
            if (tickCount[0] % 100 == 0) {
                System.out.println("[Incendiary] " + tickCount[0] + " ticks");
            }
        });

        Events.GAME_READY.register(e ->
            System.out.println("[Incendiary] GAME_READY received"));
    }
}