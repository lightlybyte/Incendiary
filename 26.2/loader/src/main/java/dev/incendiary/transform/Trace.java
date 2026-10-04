package dev.incendiary.transform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Trace {

    private static final Path FILE = Path.of(
        "C:\\Users\\light\\OneDrive\\Desktop\\Incendiary\\26.2\\agent-trace.txt");

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