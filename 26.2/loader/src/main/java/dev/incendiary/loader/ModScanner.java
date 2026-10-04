package dev.incendiary.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.incendiary.api.Side;
import dev.incendiary.transform.Trace;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Scans {@code <instanceRoot>/imods/} for mod jars and parses
 * {@code META-INF/incendiary.mod.json}. Never throws for bad mods; warns and skips.
 */
public final class ModScanner {

    private static final String META_PATH = "META-INF/incendiary.mod.json";

    private ModScanner() {}

    public static List<ModCandidate> scan(Path instanceRoot) {
        List<ModCandidate> out = new ArrayList<>();
        Path dir = instanceRoot.resolve("imods");
        if (!Files.isDirectory(dir)) {
            Trace.log("[Incendiary] imods/ not found at " + dir + ", no mods discovered");
            return out;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
            for (Path jar : stream) {
                ModCandidate c = parse(jar);
                if (c != null) out.add(c);
            }
        } catch (Throwable t) {
            Trace.log("[Incendiary] mod scan failed: " + t);
        }
        out.sort(Comparator.comparing(ModCandidate::id));
        return out;
    }

    private static ModCandidate parse(Path jar) {
        try (JarFile jf = new JarFile(jar.toFile())) {
            ZipEntry e = jf.getEntry(META_PATH);
            if (e == null) {
                Trace.log("[Incendiary] skipping " + jar.getFileName() + ": missing " + META_PATH);
                return null;
            }
            JsonObject o;
            try (InputStream in = jf.getInputStream(e);
                 InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                o = JsonParser.parseReader(r).getAsJsonObject();
            }
            String id = str(o, "id", "");
            if (!id.matches("[a-z0-9_-]{1,64}")) {
                Trace.log("[Incendiary] skipping " + jar.getFileName() + ": bad id '" + id + "'");
                return null;
            }
            String entrypoint = str(o, "entrypoint", "");
            if (entrypoint.isEmpty()) {
                Trace.log("[Incendiary] skipping mod " + id + ": empty entrypoint");
                return null;
            }
            String name = str(o, "name", id);
            String version = str(o, "version", "0.0.0");
            Side side = parseSide(str(o, "side", "both"), id, jar);
            Map<String, String> depends = new HashMap<>();
            JsonElement depEl = o.get("depends");
            if (depEl != null && depEl.isJsonObject()) {
                for (Map.Entry<String, JsonElement> en : depEl.getAsJsonObject().entrySet()) {
                    depends.put(en.getKey(), en.getValue().getAsString());
                }
            }
            return new ModCandidate(jar, id, name, version, entrypoint, side, Map.copyOf(depends));
        } catch (Throwable t) {
            Trace.log("[Incendiary] skipping " + jar.getFileName() + ": " + t);
            return null;
        }
    }

    private static String str(JsonObject o, String key, String def) {
        JsonElement e = o.get(key);
        return (e != null && e.isJsonPrimitive()) ? e.getAsString() : def;
    }

    private static Side parseSide(String raw, String id, Path jar) {
        switch (raw.toLowerCase(Locale.ROOT)) {
            case "client": return Side.CLIENT;
            case "server": return Side.SERVER;
            case "both": return Side.BOTH;
            default:
                Trace.log("[Incendiary] mod " + id + " in " + jar.getFileName()
                    + ": unknown side '" + raw + "', defaulting to both");
                return Side.BOTH;
        }
    }
}
