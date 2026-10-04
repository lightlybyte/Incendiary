package dev.incendiary.transform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Trace {

    private static final Path FILE = resolveTracePath();

    // System property override for debugging; default keeps working with no flags.
    private static Path resolveTracePath() {
        String override = System.getProperty("incendiary.trace");
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        return Path.of("C:\\Users\\light\\OneDrive\\Desktop\\Incendiary\\26.2\\agent-trace.txt");
    }

    private Trace() {}

    public static void log(String msg) {
        try {
            Files.writeString(FILE, msg + "\n",
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        } catch (Throwable ignored) {}
    }

    public static void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (Throwable ignored) {}
    }
}