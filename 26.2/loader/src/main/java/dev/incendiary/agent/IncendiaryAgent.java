package dev.incendiary.agent;

import java.lang.instrument.Instrumentation;
import dev.incendiary.transform.HookRegistry;
import dev.incendiary.transform.Transformer;

public final class IncendiaryAgent {

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[Incendiary] agent attached");

        // Register hooks BEFORE installing the transformer.
        // Format: (owner, method name, method descriptor, callback class internal name)
        // net.minecraft.client.Minecraft#tick()V
        HookRegistry.register(
            "net/minecraft/client/Minecraft",
            "tick",
            "()V",
            "dev/incendiary/hooks/LifecycleHook"
        );

        inst.addTransformer(new Transformer(), false); // false = can't retransform, fine for now
        System.out.println("[Incendiary] transformer installed");
    }
}