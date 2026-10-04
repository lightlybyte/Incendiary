package dev.incendiary.transform;

import dev.incendiary.api.Side;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HookRegistry {

    public record Target(String owner, String name, String desc) {}

    public enum Kind { HEAD, RETURN_MODIFY_STRING, OVERWRITE_VOID }

    public record HookSpec(String callbackClass, String callbackMethod, String callbackDesc, Kind kind, Side side) {}

    private static final Map<Target, List<HookSpec>> HOOKS = new HashMap<>();
    private static final java.util.Set<String> OWNERS = new java.util.HashSet<>();

    private HookRegistry() {}

    public static void registerHead(String owner, String name, String desc,
                                    String callbackClass, Side side) {
        if (!Environment.shouldApply(side)) {
            return;
        }
        HOOKS.computeIfAbsent(new Target(owner, name, desc), k -> new ArrayList<>())
             .add(new HookSpec(callbackClass, "inject", "()V", Kind.HEAD, side));
        OWNERS.add(owner);
    }

    public static void register(String owner, String name, String desc,
                                String callbackClass, Side side) {
        registerHead(owner, name, desc, callbackClass, side);
    }

    public static void registerReturnModifyString(String owner, String name, String desc,
                                                  String callbackClass, String callbackMethod, Side side) {
        if (!Environment.shouldApply(side)) {
            return;
        }
        HOOKS.computeIfAbsent(new Target(owner, name, desc), k -> new ArrayList<>())
             .add(new HookSpec(callbackClass, callbackMethod,
                 "(Ljava/lang/String;)Ljava/lang/String;", Kind.RETURN_MODIFY_STRING, side));
        OWNERS.add(owner);
    }

    /**
     * Registration-only no-op hook: the injector replaces the whole ()V body with
     * a single RETURN and never calls a callback. Used for LazyDFU-style deferral.
     * Only void descriptors are supported.
     */
    public static void registerOverwriteVoid(String owner, String name, String desc, Side side) {
        if (!desc.equals("()V")) {
            throw new IllegalArgumentException(
                "registerOverwriteVoid only supports ()V, got " + desc);
        }
        if (!Environment.shouldApply(side)) {
            return;
        }
        HOOKS.computeIfAbsent(new Target(owner, name, desc), k -> new ArrayList<>())
             .add(new HookSpec("", "", desc, Kind.OVERWRITE_VOID, side));
        OWNERS.add(owner);
    }
    public static void registerBoth(String owner, String name, String desc,
                                    String callbackClass) {
        registerHead(owner, name, desc, callbackClass, Side.BOTH);
    }

    public static boolean hasHooks(String owner) {
        return OWNERS.contains(owner);
    }

    public static List<HookSpec> hooksFor(String owner, String name, String desc) {
        List<HookSpec> hooks = HOOKS.get(new Target(owner, name, desc));
        if (hooks == null || hooks.isEmpty()) return List.of();
        return List.copyOf(hooks);
    }

    public static List<String> callbacksFor(String owner, String name, String desc) {
        List<HookSpec> hooks = HOOKS.get(new Target(owner, name, desc));
        if (hooks == null || hooks.isEmpty()) return List.of();

        List<String> result = new ArrayList<>(hooks.size());
        for (HookSpec h : hooks) {
            result.add(h.callbackClass());
        }
        return result;
    }

    public static int size() {
        return HOOKS.size();
    }

    public static void clear() {
        HOOKS.clear();
        OWNERS.clear();
    }
}