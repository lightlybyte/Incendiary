package dev.incendiary.transform;

import dev.incendiary.transform.HookRegistry.HookSpec;
import dev.incendiary.transform.HookRegistry.Kind;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class HookInjector extends ClassVisitor {

    private String currentClass;
    private int injectedCount = 0;

    public HookInjector(ClassVisitor next) {
        super(Opcodes.ASM9, next);
    }

    public int getInjectedCount() {
        return injectedCount;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        this.currentClass = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        List<HookSpec> hooks = HookRegistry.hooksFor(currentClass, name, desc);

        if (!hooks.isEmpty()) {
            Trace.log("[Incendiary] matched hook in " + currentClass
                + "." + name + desc + " hooks=" + hooks.size());
        }

        if (hooks.isEmpty()) return mv;

        // Never instrument constructors with HEAD calls: inserting before the
        // super()/this() call fails verification (uninitializedThis).
        if (name.equals("<init>") || name.equals("<clinit>")) return mv;

        final java.util.List<HookSpec> headHooks = new java.util.ArrayList<>();
        final java.util.List<HookSpec> retHooks = new java.util.ArrayList<>();
        for (HookSpec h : hooks) {
            if (h.kind() == Kind.HEAD) headHooks.add(h);
            else retHooks.add(h);
        }

        injectedCount += hooks.size();

        return new MethodVisitor(Opcodes.ASM9, mv) {
            @Override
            public void visitCode() {
                super.visitCode();
                for (HookSpec h : headHooks) {
                    Trace.log("[Incendiary] injecting call to " + h.callbackClass() + "." + h.callbackMethod() + h.callbackDesc());
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        h.callbackClass(),
                        h.callbackMethod(),
                        h.callbackDesc(),
                        false
                    );
                }
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.ARETURN && !retHooks.isEmpty()) {
                    for (HookSpec h : retHooks) {
                        Trace.log("[Incendiary] injecting return-modify " + h.callbackClass() + "." + h.callbackMethod() + h.callbackDesc());
                        super.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            h.callbackClass(),
                            h.callbackMethod(),
                            h.callbackDesc(),
                            false
                        );
                    }
                }
                super.visitInsn(opcode);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                // COMPUTE_MAXS recomputes stack sizes, so pass through.
                super.visitMaxs(maxStack, maxLocals);
            }
        };
    }
}