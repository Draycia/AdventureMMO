package me.mrdaniel.adventuremmo.event;

import javax.annotation.Nonnull;

import me.mrdaniel.adventuremmo.io.playerdata.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PlayerDataEvent extends AbstractEvent {

    private final PlayerData data;
    private final Cause cause = Sponge.getCauseStackManager().getCurrentCause();

    public PlayerDataEvent(PlayerData data) {
        this.data = data;
    }

    @Nonnull
    public PlayerData getPlayerData() {
        return this.data;
    }

    @Nonnull
    @Override
    public Cause getCause() {
        return this.cause;
    }

    public static class Load extends PlayerDataEvent {
        public Load(PlayerData data) {
            super(data);
        }
    }

    public static class Unload extends PlayerDataEvent {
        public Unload(PlayerData data) {
            super(data);
        }
    }
}