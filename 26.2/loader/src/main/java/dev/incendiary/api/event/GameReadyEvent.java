package dev.incendiary.api.event;

public final class GameReadyEvent {
    public static final GameReadyEvent INSTANCE = new GameReadyEvent();
    private GameReadyEvent() {}
}