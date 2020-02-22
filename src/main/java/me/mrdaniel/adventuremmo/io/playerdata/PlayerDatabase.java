package me.mrdaniel.adventuremmo.io.playerdata;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.spongepowered.api.Sponge;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.event.LevelUpEvent;
import me.mrdaniel.adventuremmo.utils.MathUtils;

public interface PlayerDatabase {

    AdventureMMO getPlugin();

    void unload(UUID uuid);

    void unloadAll();

    PlayerData get(UUID uuid);

    Optional<PlayerData> getOffline(UUID uuid);

    default void addExp(@Nonnull final PlayerData playerData, @Nonnull final SkillType skill, final int exp) {
        int current_level = playerData
                .getLevel(
                        skill);

        int current_exp = playerData.getExp(skill);
        int new_exp = current_exp + exp;
        int exp_till_next_level = MathUtils.expTillNextLevel(current_level);

        if (new_exp >= exp_till_next_level) {
            if (!Sponge.getGame().getEventManager().post(new LevelUpEvent(
                            playerData,
                            skill,
                            current_level,
                            current_level + 1,
                            Sponge.getCauseStackManager().getCurrentCause()))) {

                playerData.setLevel(skill, current_level + 1);
                new_exp -= exp_till_next_level;
            }
        }

        // TODO: EXPGainEvent
        playerData.setExp(skill, new_exp);
    }
}