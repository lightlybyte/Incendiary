# Incendiary API — Events

**PLAYER_JOIN and PLAYER_LEAVE are declared but do not fire yet. They are
reserved for the next release.**

## Event list

- `Events.CLIENT_TICK` — fires every client tick.
- `Events.GAME_READY` — fires once on the first tick.
- `Events.PLAYER_JOIN` — declared, not fired.
- `Events.PLAYER_LEAVE` — declared, not fired.

## Event<T>

```java
public final class Event<T> {
    public void register(java.util.function.Consumer<T> listener);
    public void fire(T payload);
}
```

> Mods must not call `fire` — only `register`. Firing is reserved for the
> loader's hooks.

Listeners run on the game thread, in registration order. Exceptions propagate
to the game loop — do not throw from listeners. There is no unregister.

```java
Events.CLIENT_TICK.register(e -> { /* per tick */ });
```

[← Back to index](README.md)
