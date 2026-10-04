package dev.incendiary.loader;

import dev.incendiary.transform.Trace;
import java.nio.file.Path;
import java.util.List;

/**
 * Facade: scan, resolve order, log one summary line per mod. No classloading here;
 * entrypoint instantiation happens later on the game thread.
 */
public final class ModDiscovery {

    private ModDiscovery() {}

    public static List<ModCandidate> discover(Path instanceRoot) {
        List<ModCandidate> scanned = ModScanner.scan(instanceRoot);
        List<ModCandidate> ordered = DependencyResolver.resolve(scanned);
        for (ModCandidate m : ordered) {
            Trace.log("[Incendiary] found mod " + m.id() + " " + m.version()
                + " (side=" + m.side().name().toLowerCase(java.util.Locale.ROOT)
                + ", entry=" + m.entrypoint() + ")");
        }
        return ordered;
    }
}
