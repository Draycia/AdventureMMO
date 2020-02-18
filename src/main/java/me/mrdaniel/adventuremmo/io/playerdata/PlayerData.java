package me.mrdaniel.adventuremmo.io.playerdata;

import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillTypes;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public interface PlayerData {

    int getExp(SkillType skill);

    void setExp(SkillType skill, final int exp);

    int getLevel(SkillType skill);

    void setLevel(SkillType skill, final int level);

    default int getLevels() {
        return SkillTypes.VALUES.stream().mapToInt(this::getLevel).sum();
    }

    long getLastUse();

    void save();

    UUID getPlayerUUID();

    Player getPlayer();
}