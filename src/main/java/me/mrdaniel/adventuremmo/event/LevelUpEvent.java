package me.mrdaniel.adventuremmo.event;

import javax.annotation.Nonnull;

import me.mrdaniel.adventuremmo.io.playerdata.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;

public class LevelUpEvent extends AbstractEvent implements Cancellable {

    private final PlayerData player;
    private final SkillType skill;
    private final int old_level;
    private final int new_level;

    private final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
    private boolean cancelled;

    public LevelUpEvent(PlayerData player, SkillType skill, int old_level, int new_level) {
        this.player = player;
        this.skill = skill;
        this.old_level = old_level;
        this.new_level = new_level;
        this.cancelled = false;
    }

    @Nonnull
    public PlayerData getPlayerData() {
        return this.player;
    }

    @Nonnull
    public SkillType getSkill() {
        return this.skill;
    }

    public int getOldLevel() {
        return this.old_level;
    }

    public int getNewLevel() {
        return this.new_level;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}