# Incendiary API — Metadata

`META-INF/incendiary.mod.json` (UTF-8) inside the jar.

## Fields

- `id` (string, required): `[a-z0-9_-]{1,64}`.
- `name` (string, optional): display name, defaults to `id`.
- `version` (string, required): opaque string, defaults to `0.0.0` if absent.
- `entrypoint` (string, required): FQCN implementing `IncendiaryMod`.
- `side` (string, optional): `client` | `server` | `both`, default `both`.
- `depends` (object, optional): mod id to version range, default `{}`.
  Only `incendiary` with a leading `>=` is enforced.

## Failure modes

All failures skip the jar with a trace line; none throw:

- missing file: skipped.
- invalid JSON: skipped.
- bad id: skipped.
- empty entrypoint: skipped.
- unknown side: defaults to `both` with a trace warning.
- unsatisfied `incendiary >=` range: skipped with
  `mod <id> requires incendiary <range>, have <VERSION>, skipping`.
- missing mod dependency or cycle: input order kept with a trace warning.

## Example

```json
{
  "id": "examplemod",
  "name": "Example Mod",
  "version": "1.0.0",
  "entrypoint": "com.example.ExampleMod",
  "side": "both",
  "depends": { "incendiary": ">=1.0.0" }
}
```

[← Back to index](README.md)
