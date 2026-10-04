package dev.incendiary.hooks;

/**
 * Marker for the LazyDFU-style no-op hook. No methods: the injector replaces
 * the target body, so no callback is ever called.
 */
public final class LazyDfuHook {
    private LazyDfuHook() {}
}
