# Incendiary API — Getting started

Minimal mod from zero.

## Layout

```
mymod/
  build.gradle.kts
  libs/incendiary-loader.jar
  src/main/java/com/example/ExampleMod.java
  src/main/resources/META-INF/incendiary.mod.json
```

## Build file

Until the `incendiary-api` artifact ships, compile against the loader jar:

```kotlin
dependencies {
    compileOnly(files("libs/incendiary-loader.jar"))
}
```

## Mod class

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

## Metadata

See [metadata](metadata.md) for the full schema. Minimal file at
`src/main/resources/META-INF/incendiary.mod.json`:

```json
{
  "id": "examplemod",
  "version": "1.0.0",
  "entrypoint": "com.example.ExampleMod"
}
```

## Install and launch

Build the jar and drop it in:

```
C:\Users\light\AppData\Roaming\.minecraft\imods\examplemod-1.0.0.jar
```

Note the folder is **imods**, not `mods`. Launch through SKLauncher with
`-javaagent:.../incendiary-loader.jar` in the JVM arguments.

[← Back to index](README.md)
