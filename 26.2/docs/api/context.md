# Incendiary API — Context

```java
package dev.incendiary.api;

public interface ModContext {
    String modId();
    org.slf4j.Logger logger();
    Side side();
    java.nio.file.Path configDir();
}
```

- `modId()` — id from `incendiary.mod.json`.
- `logger()` — SLF4J logger scoped to this mod. Log through it, not stdout.
- `side()` — runtime side.
- `configDir()` — lazy-created on first call; returns
  `<instance>/config/<modId>/`; throws `UncheckedIOException` wrapping the
  underlying `IOException` if the directory cannot be created.

[← Back to index](README.md)
