package dev.incendiary.api.event;

/**
 * Fired when a player leaves. Declared but not yet fired by any hook.
 */
public final class PlayerLeaveEvent {

    public static final PlayerLeaveEvent INSTANCE = new PlayerLeaveEvent();

    /** Nullable; empty string if unknown. */
    public final String playerName;

    public PlayerLeaveEvent() {
        this.playerName = "";
    }

    public PlayerLeaveEvent(String playerName) {
        this.playerName = playerName == null ? "" : playerName;
    }

    public static PlayerLeaveEvent withPlayerName(String playerName) {
        return new PlayerLeaveEvent(playerName);
    }
}
