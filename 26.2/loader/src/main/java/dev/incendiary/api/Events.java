package dev.incendiary.api;

import dev.incendiary.api.event.ClientTickEvent;
import dev.incendiary.api.event.GameReadyEvent;

public final class Events {

    public static final Event<ClientTickEvent> CLIENT_TICK = new Event<>();
    public static final Event<GameReadyEvent> GAME_READY = new Event<>();

    private Events() {}
}