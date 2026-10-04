package dev.incendiary.api;

import dev.incendiary.api.event.ClientTickEvent;
import dev.incendiary.api.event.GameReadyEvent;
import dev.incendiary.api.event.PlayerJoinEvent;
import dev.incendiary.api.event.PlayerLeaveEvent;

public final class Events {

    public static final Event<ClientTickEvent> CLIENT_TICK = new Event<>();
    public static final Event<GameReadyEvent> GAME_READY = new Event<>();

    /** Declared for API stability; no hook fires this yet. */
    public static final Event<PlayerJoinEvent> PLAYER_JOIN = new Event<>();

    /** Declared for API stability; no hook fires this yet. */
    public static final Event<PlayerLeaveEvent> PLAYER_LEAVE = new Event<>();

    private Events() {}
}