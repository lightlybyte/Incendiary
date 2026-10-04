# Incendiary API — Entrypoint

```java
package dev.incendiary.api;

public interface IncendiaryMod {
    void onInitialize(ModContext ctx);
}
```

Contract: called once per mod, on the game thread, in dependency order.
Do not block. Do not throw. See [context](context.md) for what `ctx` offers.

```java
package com.example;

import dev.incendiary.api.IncendiaryMod;
import dev.incendiary.api.ModContext;

public final class ExampleMod implements IncendiaryMod {
    @Override
    public void onInitialize(ModContext ctx) {
        ctx.logger().info("examplemod initialized on {}", ctx.side());
    }
}
```

[← Back to index](README.md)
