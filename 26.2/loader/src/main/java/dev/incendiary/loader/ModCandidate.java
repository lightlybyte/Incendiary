package dev.incendiary.loader;

import dev.incendiary.api.Side;
import java.nio.file.Path;
import java.util.Map;

/**
 * Parsed metadata for one mod jar found in {@code <instanceRoot>/imods/}.
 */
public record ModCandidate(
    Path jarPath,
    String id,
    String name,
    String version,
    String entrypoint,
    Side side,
    Map<String, String> depends
) {}
