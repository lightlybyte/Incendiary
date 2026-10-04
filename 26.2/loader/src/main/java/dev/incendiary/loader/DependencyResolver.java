package dev.incendiary.loader;

import dev.incendiary.api.IncendiaryApi;
import dev.incendiary.transform.Trace;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orders mods so dependencies load first. Only the "incendiary" dependency id is
 * enforced (a leading "&gt;=" range is checked against {@link IncendiaryApi#VERSION});
 * all other ids are ordering hints. Failures warn and return input order unchanged.
 * A full semver solver is v2 work.
 */
public final class DependencyResolver {

    private DependencyResolver() {}

    public static List<ModCandidate> resolve(List<ModCandidate> mods) {
        if (mods.isEmpty()) return List.of();

        // v1 enforcement: drop mods whose incendiary >= range is not satisfied.
        List<ModCandidate> kept = new ArrayList<>(mods);
        kept.removeIf(m -> {
            String req = m.depends().get("incendiary");
            if (req != null && req.startsWith(">=")) {
                String want = req.substring(2).trim();
                if (compareVersions(IncendiaryApi.VERSION, want) < 0) {
                    Trace.log("[Incendiary] mod " + m.id() + " requires incendiary " + req
                        + ", have " + IncendiaryApi.VERSION + ", skipping");
                    return true;
                }
            }
            return false;
        });
        if (kept.isEmpty()) return List.of();
        mods = kept;

        // Kahn's algorithm over mod-id edges only (ignore external ids like "incendiary").
        Map<String, ModCandidate> byId = new HashMap<>();
        for (ModCandidate m : mods) byId.put(m.id(), m);
        Map<String, Set<String>> deps = new HashMap<>();
        Map<String, Set<String>> dependents = new HashMap<>();
        for (ModCandidate m : mods) {
            deps.put(m.id(), new HashSet<>());
            dependents.put(m.id(), new HashSet<>());
        }
        for (ModCandidate m : mods) {
            for (String dep : m.depends().keySet()) {
                if (dep.equals("incendiary") || dep.equals("minecraft")) continue;
                if (!byId.containsKey(dep)) {
                    Trace.log("[Incendiary] mod " + m.id() + " missing dependency '" + dep
                        + "'; keeping input order");
                    return List.copyOf(mods);
                }
                deps.get(m.id()).add(dep);
                dependents.get(dep).add(m.id());
            }
        }
        List<ModCandidate> sorted = new ArrayList<>(mods.size());
        List<String> ready = new ArrayList<>();
        for (Map.Entry<String, Set<String>> e : deps.entrySet()) {
            if (e.getValue().isEmpty()) ready.add(e.getKey());
        }
        ready.sort(Comparator.naturalOrder());
        while (!ready.isEmpty()) {
            String id = ready.remove(0);
            sorted.add(byId.get(id));
            List<String> next = new ArrayList<>(dependents.get(id));
            next.sort(Comparator.naturalOrder());
            for (String dependent : next) {
                deps.get(dependent).remove(id);
                if (deps.get(dependent).isEmpty()) ready.add(dependent);
            }
            ready.sort(Comparator.naturalOrder());
        }
        if (sorted.size() != mods.size()) {
            Trace.log("[Incendiary] dependency cycle detected; keeping input order");
            return List.copyOf(mods);
        }
        return sorted;
    }

    // Numeric dot-separated compare; non-numeric tails ignored. Full semver is v2.
    static int compareVersions(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            int na = i < pa.length ? num(pa[i]) : 0;
            int nb = i < pb.length ? num(pb[i]) : 0;
            if (na != nb) return Integer.compare(na, nb);
        }
        return 0;
    }

    private static int num(String part) {
        int i = 0;
        while (i < part.length() && Character.isDigit(part.charAt(i))) i++;
        return i == 0 ? 0 : Integer.parseInt(part.substring(0, i));
    }
}
