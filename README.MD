# Incendiary Loader — Design

## Target
- Minecraft 26.2+ (unobfuscated)
- Java 25
- Fabric Mixin fork `0.17.4+mixin.0.8.7`

## Motives
1. Zero inherited debt. No forks of existing loaders.
2. Single MC version per loader release.
3. Unobfuscated only. No remapping, no refmaps.
4. Own the API. Third-party mods out of scope for v1.
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

## API surface (v1)
- `IncendiaryMod` interface (entrypoint)
- `ModContext` passed to onInitialize
- `Event<T>` bus with four events: CLIENT_TICK, SERVER_TICK, PLAYER_JOIN, PLAYER_LEAVE
- `Side` enum: CLIENT, SERVER, BOTH

## Metadata: incendiary.mod.json
{ id, name, version, entrypoints.main, depends, side }

## Classloading
mod classloader → api classloader → platform classloader

## Lifecycle
premain → scan mods → resolve deps → install Mixin transformer
→ MC classes load → transform → lifecycle signal → onInitialize
→ events fire

## Performance rules
- No per-tick allocation in event dispatch
- No injection into hot loops
- Pre-resolve listeners to arrays at registration
- Loader must be invisible in a profiler

## v1 done criterion
A test mod loads from mods/, prints "[testmod] initialized on CLIENT",
and increments a counter every CLIENT_TICK.