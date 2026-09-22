package dev.incendiary.transform;

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

        List<String> callbacks = HookRegistry.callbacksFor(currentClass, name, desc);
        if (callbacks.isEmpty()) return mv;

        injectedCount += callbacks.size();

        return new MethodVisitor(Opcodes.ASM9, mv) {
            @Override
            public void visitCode() {
                super.visitCode();
                for (String callback : callbacks) {
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        callback,
                        "inject",
                        "()V",
                        false
                    );
                }
            }
        };
    }
}