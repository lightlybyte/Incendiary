package dev.incendiary.transform;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public final class Transformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain pd, byte[] classfileBuffer) {
        if (className == null) return null;

        Trace.log("[Incendiary] transform called for " + className);

        if (!HookRegistry.hasHooks(className)) return null;

        Trace.log("[Incendiary] transforming " + className);

        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            HookInjector injector = new HookInjector(cw);
            cr.accept(injector, ClassReader.EXPAND_FRAMES);

            Trace.log("[Incendiary] " + className
                + " injected " + injector.getInjectedCount());

            if (injector.getInjectedCount() == 0) return null;
            return cw.toByteArray();
        } catch (Throwable t) {
            Trace.log("[Incendiary] EXCEPTION transforming " + className);
            Trace.log(t.toString());
            for (StackTraceElement e : t.getStackTrace()) {
                Trace.log("  at " + e);
            }
            return null;
        }
    }
}