package dev.incendiary.agent;

import java.lang.instrument.Instrumentation;
import dev.incendiary.api.Side;
import dev.incendiary.transform.Environment;
import dev.incendiary.transform.HookRegistry;
import dev.incendiary.transform.Trace;
import dev.incendiary.transform.Transformer;

public final class IncendiaryAgent {

    public static void premain(String args, Instrumentation inst) {
        Trace.clear();
        Trace.log("[Incendiary] premain entered");
        Trace.log("[Incendiary] side: " + Environment.current());

        HookRegistry.register(
            "net/minecraft/client/Minecraft",
            "tick",
            "()V",
            "dev/incendiary/hooks/LifecycleHook",
            Side.CLIENT
        );

        HookRegistry.registerReturnModifyString(
            "net/minecraft/client/Minecraft",
            "createTitle",
            "()Ljava/lang/String;",
            "dev/incendiary/hooks/TitleHook",
            "modify",
            Side.CLIENT
        );

        Trace.log("[Incendiary] registry size: " + HookRegistry.size());
        Trace.log("[Incendiary] hasHooks(Minecraft): "
            + HookRegistry.hasHooks("net/minecraft/client/Minecraft"));

        inst.addTransformer(new Transformer(), false);
        Trace.log("[Incendiary] transformer installed");
    }
}