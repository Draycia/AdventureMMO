package me.mrdaniel.adventuremmo.io.playerdata;

import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import javax.sql.DataSource;
import java.util.UUID;

public class DummySqlPlayerData extends SQLPlayerData {

    private UUID playerUUID;

    public DummySqlPlayerData(UUID playerUUID, DataSource dataSource) {
        super(playerUUID, dataSource);

        this.playerUUID = playerUUID;
    }

    @Override
    public int getExp(SkillType skill) {
        return 0;
    }

    @Override
    public void setExp(SkillType skill, int exp) {

    }

    @Override
    public int getLevel(SkillType skill) {
        return 0;
    }

    @Override
    public void setLevel(SkillType skill, int level) {

    }

    @Override
    public long getLastUse() {
        return 0;
    }

    @Override
    public void save() { }

    @Override
    public UUID getPlayerUUID() {
        return null;
    }

    @Override
    public Player getPlayer() {
        return Sponge.getServer().getPlayer(playerUUID).get();
    }

}
