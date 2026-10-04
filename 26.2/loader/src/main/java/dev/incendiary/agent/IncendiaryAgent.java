package dev.incendiary.agent;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.List;
import dev.incendiary.api.Side;
import dev.incendiary.loader.ModCandidate;
import dev.incendiary.loader.ModDiscovery;
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

        // LazyDFU note: verified 26.2 target is
        // DataFixers.optimize(Ljava/util/Set;)Ljava/util/concurrent/CompletableFuture;
        // which is NOT ()V, so registerOverwriteVoid cannot take it (void-only by
        // design). No ()V DFU trigger exists to no-op safely, so no hook is
        // registered: the game pays the normal DFU cost, which is the safe fallback.
        // LazyDFU defers DFU; it never strips it (DataBreaker-style strips corrupt).
        Trace.log("[Incendiary] DFU no-op hook skipped: no ()V target on 26.2");

        Trace.log("[Incendiary] registry size: " + HookRegistry.size());
        Trace.log("[Incendiary] hasHooks(Minecraft): "
            + HookRegistry.hasHooks("net/minecraft/client/Minecraft"));

        try {
            Path instanceRoot = Path.of(System.getProperty("user.home"),
                "AppData", "Roaming", ".minecraft");
            List<ModCandidate> discovered = ModDiscovery.discover(instanceRoot);
            Trace.log("[Incendiary] discovered " + discovered.size() + " mod(s)");
        } catch (Throwable t) {
            Trace.log("[Incendiary] mod discovery failed: " + t);
        }

        inst.addTransformer(new Transformer(), false);
        Trace.log("[Incendiary] transformer installed");
    }
}