package dev.incendiary.agent;

import java.lang.instrument.Instrumentation;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

public final class IncendiaryAgent {

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[Incendiary] agent attached");

        try {
            MixinBootstrap.init();
            Mixins.addConfiguration("mixins.incendiary.json");
            MixinEnvironment.getDefaultEnvironment()
                .setSide(MixinEnvironment.Side.CLIENT);
            System.out.println("[Incendiary] mixin bootstrapped");
        } catch (Throwable t) {
            System.err.println("[Incendiary] mixin bootstrap FAILED");
            t.printStackTrace();
        }
    }
}