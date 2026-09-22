package dev.incendiary.transform;

import java.util.*;

public final class HookRegistry {

    public record Target(String owner, String name, String desc) {}

    private static final Map<Target, List<String>> HOOKS = new HashMap<>();

    private HookRegistry() {}

    /**
     * Register a hook. When the method matching `target` is loaded,
     * a static call to `callbackClass.inject()` will be inserted at the
     * start of the method body.
     *
     * @param owner      internal class name, e.g. "net/minecraft/client/Minecraft"
     * @param name       method name, e.g. "tick"
     * @param desc       method descriptor, e.g. "()V"
     * @param callbackClass internal name of the class whose `inject()` method
     *                      should be called, e.g. "dev/incendiary/hooks/LifecycleHook"
     */
    public static void register(String owner, String name, String desc, String callbackClass) {
        HOOKS.computeIfAbsent(new Target(owner, name, desc), k -> new ArrayList<>())
             .add(callbackClass);
    }

    public static boolean hasHooks(String owner) {
        for (Target t : HOOKS.keySet()) {
            if (t.owner().equals(owner)) return true;
        }
        return false;
    }

    public static List<String> callbacksFor(String owner, String name, String desc) {
        return HOOKS.getOrDefault(new Target(owner, name, desc), List.of());
    }

    public static void clear() {
        HOOKS.clear();
    }
}