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
        if (!HookRegistry.hasHooks(className)) return null;

        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            HookInjector injector = new HookInjector(cw);
            cr.accept(injector, ClassReader.EXPAND_FRAMES);

            if (injector.getInjectedCount() == 0) {
                // Nothing actually matched; let the original bytes through.
                return null;
            }

            System.out.println("[Incendiary] transformed " + className
                + " (" + injector.getInjectedCount() + " hook(s))");
            return cw.toByteArray();
        } catch (Throwable t) {
            System.err.println("[Incendiary] failed to transform " + className);
            t.printStackTrace();
            return null; // fall back to original
        }
    }
}