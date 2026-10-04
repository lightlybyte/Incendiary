# Incendiary API — Index

Incendiary is a from-scratch Minecraft 26.2 mod loader. The only stable public
surface is everything under `dev.incendiary.api`. Everything else in the loader
jar is internal and may change without notice.

## Contents

- [Getting started](getting-started.md)
- [Mod metadata](metadata.md)
- [Entrypoint](entrypoint.md)
- [ModContext](context.md)
- [Events](events.md)
- [Side](side.md)
- [Stability contract](stability.md)

## Stability contract

Everything under `dev.incendiary.api` is stable within a major version. Everything
else is internal. Reflecting into internals will break. Full text in
[stability](stability.md).

## Version handshake

`IncendiaryApi.VERSION` is `"1.0.0"`, `MC_TARGET` is `"26.2"`.
Declare `"depends": { "incendiary": ">=1.0.0" }` in your metadata.

## What's not here yet

Config API, commands, network, block/item registration, resources, reload.
`loadedMods()` / `isModLoaded()` / `getMod()` return empty until v1.1.
`PLAYER_JOIN` / `PLAYER_LEAVE` are declared but do not fire yet.

## Debugging

The loader writes `agent-trace.txt` next to the loader project by default.
Override with `-Dincendiary.trace=<path>`.

[← Back to index](README.md)
