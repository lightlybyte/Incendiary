# Incendiary Loader

From-scratch Minecraft mod loader. Minecraft 26.2, Java 25, unobfuscated client —
no remapping, no refmaps, no Mixin, no Fabric/Forge inheritance. Every line owned.

## Target

- Minecraft 26.2 (unobfuscated — real class/method names, no mapping layer)
- Java 25
- Own ASM 9.8 bytecode injector via `-javaagent`

## Motives

1. Zero inherited debt. No forks of existing loaders.
2. Single MC version per loader release (26.2 now, 26.3 mechanically later).
3. Unobfuscated only. No remapping, no refmaps.
4. Own the API. Third-party mods compile against `dev.incendiary.api` only.
5. Runtime transformation only. Never edit source.
6. Explicit over implicit.
7. Trust the mods. No sandboxing.

## Non-goals

- Mixin compatibility with Fabric/Forge mods
- Mod distribution platform
- Config library
- GUI framework
- Cross-version mod compatibility
- Hot reload
- Mod signing
- Build system

## How it works

`IncendiaryAgent.premain` registers hooks in `HookRegistry`, runs mod discovery
over `<instance>/imods/`, then installs `Transformer` via
`Instrumentation.addTransformer`. On each class load the transformer checks
`HookRegistry.hasHooks` (O(1)) and runs matches through `HookInjector`:

- `HEAD` — prepend `INVOKESTATIC callback.inject()V` at method entry.
  Live: `Minecraft.tick()V` → `LifecycleHook` (fires `CLIENT_TICK` every tick,
  `GAME_READY` once).
- `RETURN_MODIFY_STRING` — insert
  `INVOKESTATIC callback.modify(Ljava/lang/String;)Ljava/lang/String;` before
  every `ARETURN`. Live: `Minecraft.createTitle()Ljava/lang/String;` →
  `TitleHook` (window title `Minecraft 26.2 - Incendiary`).
- `OVERWRITE_VOID` — replace a `()V` body with a single `RETURN`.
  Registration-only, no callback. Implemented for LazyDFU-style deferral, but
  **not wired on 26.2**: verified via `javap` that `SharedConstants` has no
  `enableDataFixerOptimizations`, and the real DFU entry
  (`DataFixers.optimize(Set)CompletableFuture`) is non-void. Game pays normal
  DFU cost — the safe fallback. Never strip DFU.

`COMPUTE_MAXS` + `EXPAND_FRAMES` throughout. Constructors never instrumented.
Diagnostics go to `agent-trace.txt` (Log4j isn't up at premain, SKLauncher
filters stdout); override with `-Dincendiary.trace=<path>`.

## API surface (v1)

Stable package: `dev.incendiary.api` — documented in [docs/api/](26.2/docs/api/README.md).

- `IncendiaryMod` — single entrypoint `onInitialize(ModContext)`
- `ModContext` — `modId()`, `logger()`, `side()`, `configDir()` (lazy-created,
  `UncheckedIOException` on failure)
- `Event<T>` / `Events` — `CLIENT_TICK` + `GAME_READY` fire;
  `PLAYER_JOIN` + `PLAYER_LEAVE` declared, not yet fired (reserved)
- `Side` — `CLIENT`, `SERVER`, `BOTH`
- `ModContainer`, `ModState` — reserved for v1.1
- `IncendiaryApi` — `VERSION = "1.0.0"`, `MC_TARGET = "26.2"`, `side()`,
  `loaderName()`, `loaderVersion()`; `loadedMods()` / `isModLoaded()` /
  `getMod()` return empty until v1.1
- `exceptions.ModLoadException`, `exceptions.ModMetadataException` — reserved
  for entrypoint loading (prompt 4)

## Metadata: incendiary.mod.json

Inside the jar at `META-INF/incendiary.mod.json`. Full schema in
[docs/api/metadata.md](26.2/docs/api/metadata.md).

`{ id, name, version, entrypoint, side, depends }` — `id` must match
`[a-z0-9_-]{1,64}`. Only `incendiary >=x.y.z` ranges enforced; unsatisfied mods
are skipped with a trace line. Bad jars skip, never throw.

## Classloading / lifecycle

Mod classloader + `onInitialize` invocation do not exist yet (prompt 4).
Today: premain → register hooks → discover `imods/` → scan, parse, resolve
order, log per mod → install transformer → MC loads → hooks fire.
Entrypoints are not loaded.

## Layout

- `26.2/client_26.2.jar`, `repl.bat`, `JRE/`, `essentials/`
- `26.2/loader/` — Gradle 9.0 project (ASM 9.8, Gson + SLF4J `compileOnly`)
- `26.2/loader/build/libs/incendiary-loader.jar` — the agent
  (`Premain-Class`, `Can-Retransform-Classes: true`)
- `26.2/docs/api/` — modder docs (index at `README.md`)
- `26.2/agent-trace.txt` — diagnostics

## Launch

SKLauncher 3.2.18, JVM args include
`-javaagent:C:\...\Incendiary\26.2\loader\build\libs\incendiary-loader.jar`.
Mods go in `C:\Users\light\AppData\Roaming\.minecraft\imods\` (**imods**, not
`mods`). Standalone agent check:
`JRE\bin\java.exe -javaagent:.../incendiary-loader.jar -version` must create
`agent-trace.txt` (shows `side: SERVER`, `registry size: 0` — correct without
client classes; in-game it is `CLIENT` / hooks matched).

## Performance rules

- No per-tick allocation in event dispatch (index loop, no iterators)
- No injection into hot loops
- Transformer silent on non-matches; per-tick file logging removed
- Loader must be invisible in a profiler

## Thanks

To everyone who has ever written a mod loader, a mod, or a tool for a game they
loved, and then let go. Especially the ones who let go well.