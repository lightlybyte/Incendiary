package dev.incendiary.api.event;

/**
 * Fired when a player joins. Declared but not yet fired by any hook.
 */
public final class PlayerJoinEvent {

    public static final PlayerJoinEvent INSTANCE = new PlayerJoinEvent();

    /** Nullable; empty string if unknown. */
    public final String playerName;

    public PlayerJoinEvent() {
        this.playerName = "";
    }

    public PlayerJoinEvent(String playerName) {
        this.playerName = playerName == null ? "" : playerName;
    }

    public static PlayerJoinEvent withPlayerName(String playerName) {
        return new PlayerJoinEvent(playerName);
    }
}
