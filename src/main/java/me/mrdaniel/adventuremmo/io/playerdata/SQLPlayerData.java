package me.mrdaniel.adventuremmo.io.playerdata;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillData;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SQLPlayerData implements PlayerData {

    private UUID playerUUID;
    private DataSource dataSource;
    private long last_use;

    private ConcurrentHashMap<SkillType, SkillData> skillData = new ConcurrentHashMap<>();

    public SQLPlayerData(UUID playerUUID, DataSource dataSource) {
        this.playerUUID = playerUUID;
        this.dataSource = dataSource;
        this.last_use = System.currentTimeMillis();
    }

    @Override
    public int getExp(@Nonnull final SkillType skill) {
        this.setLastUse();

        SkillData data = skillData.get(skill);

        if (data == null) {
            return 0;
        }

        return data.getExperience();
    }

    @Override
    public void setExp(@Nonnull final SkillType skill, final int exp) {
        this.setLastUse();

        SkillData data = skillData.get(skill);

        if (data == null) {
            data = new SkillData(0, exp);
            skillData.put(skill, data);
        } else {
            data.setExperience(exp);
        }
    }

    @Override
    public int getLevel(@Nonnull final SkillType skill) {
        this.setLastUse();

        SkillData data = skillData.get(skill);

        if (data == null) {
            return 0;
        }

        return data.getLevel();
    }

    @Override
    public void setLevel(@Nonnull final SkillType skill, final int level) {
        this.setLastUse();

        SkillData data = skillData.get(skill);

        if (data == null) {
            data = new SkillData(level, 0);
            skillData.put(skill, data);
        } else {
            data.setLevel(level);
        }
    }

    @Override
    public long getLastUse() {
        return this.last_use;
    }

    private void setLastUse() {
        this.last_use = System.currentTimeMillis();
    }

    @Override
    public void save() {
        try (Connection connection = dataSource.getConnection()) {
            for (Map.Entry<SkillType, SkillData> entry : skillData.entrySet()) {
                try (PreparedStatement statement = connection.prepareStatement("REPLACE into skill_" + entry.getKey().getId() + " (id_lsig, ld_msig, experience, level) values(?, ?, ?, ?)")) {
                    statement.setLong(1, playerUUID.getLeastSignificantBits());
                    statement.setLong(2, playerUUID.getMostSignificantBits());
                    statement.setInt(3, getExp(entry.getKey()));
                    statement.setInt(4, getLevel(entry.getKey()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public Player getPlayer() {
        return Sponge.getServer().getPlayer(getPlayerUUID()).get();
    }
}