package dev.incendiary.transform;

import dev.incendiary.api.Side;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HookRegistry {

    public record Target(String owner, String name, String desc) {}

    private record Hook(String callbackClass, Side side) {}

    private static final Map<Target, List<Hook>> HOOKS = new HashMap<>();

    private HookRegistry() {}

    /**
     * Register a hook.
     *
     * When the method matching (owner, name, desc) is loaded by the JVM,
     * a static call to {@code callbackClass.inject()V} will be inserted
     * at the start of the method body.
     *
     * Hooks registered with a side that doesn't match the current runtime
     * environment are silently dropped. This means a mod can declare its
     * hooks unconditionally, and the loader takes care of the rest.
     *
     * @param owner         internal class name, e.g. "net/minecraft/client/Minecraft"
     * @param name          method name, e.g. "tick"
     * @param desc          method descriptor, e.g. "()V"
     * @param callbackClass internal class name of the callback, e.g.
     *                      "dev/incendiary/hooks/LifecycleHook"
     * @param side          which runtime side this hook applies to
     */
    public static void register(String owner, String name, String desc,
                                String callbackClass, Side side) {
        if (!Environment.shouldApply(side)) {
            return;
        }
        HOOKS.computeIfAbsent(new Target(owner, name, desc), k -> new ArrayList<>())
             .add(new Hook(callbackClass, side));
    }

    /**
     * Convenience overload for hooks that apply to both sides.
     */
    public static void registerBoth(String owner, String name, String desc,
                                    String callbackClass) {
        register(owner, name, desc, callbackClass, Side.BOTH);
    }

    /**
     * Returns true if any hook is registered against the given class.
     * Called by the Transformer to decide whether to bother reading a class
     * at all. This is the hot path — every class the JVM loads checks here.
     */
    public static boolean hasHooks(String owner) {
        for (Target t : HOOKS.keySet()) {
            if (t.owner().equals(owner)) return true;
        }
        return false;
    }

    /**
     * Returns the callback class names registered for a specific method,
     * in registration order. Only called during class transformation, not
     * on any hot path, so allocation here is acceptable.
     */
    public static List<String> callbacksFor(String owner, String name, String desc) {
        List<Hook> hooks = HOOKS.get(new Target(owner, name, desc));
        if (hooks == null || hooks.isEmpty()) return List.of();

        List<String> result = new ArrayList<>(hooks.size());
        for (Hook h : hooks) {
            result.add(h.callbackClass());
        }
        return result;
    }

    /**
     * Removes every registered hook. Intended for tests and reloading
     * scenarios. Not called by the loader itself.
     */
    public static void clear() {
        HOOKS.clear();
    }
}