package me.mrdaniel.adventuremmo.io.playerdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public class HoconPlayerData implements PlayerData {

    private static final Logger LOGGER = LoggerFactory.getLogger("AdventureMMO Playerdata");

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final CommentedConfigurationNode node;

    private long last_use;

    private UUID playerUUID;

    public HoconPlayerData(@Nonnull final Path path, UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.loader = HoconConfigurationLoader.builder().setPath(path).build();

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (final IOException exc) {
                LOGGER.error("Failed to create playerdata file: {}", exc);
            }
        }
        this.node = this.load();
        this.last_use = System.currentTimeMillis();
    }

    private CommentedConfigurationNode load() {
        try {
            return this.loader.load();
        } catch (final IOException exc) {
            LOGGER.error("Failed to load playerdata file: {}", exc);
            return this.loader.createEmptyNode();
        }
    }

    @Override
    public void save() {
        try {
            this.loader.save(this.node);
        } catch (final IOException exc) {
            LOGGER.error("Failed to save playerdata file: {}", exc);
        }
    }

    @Override
    public int getLevel(@Nonnull final SkillType skill) {
        this.setLastUse();
        return this.node.getNode(skill.getId(), "level").getInt();
    }

    @Override
    public void setLevel(@Nonnull final SkillType skill, final int level) {
        this.setLastUse();
        node.getNode(skill.getId(), "level").setValue(level);
    }

    @Override
    public int getExp(@Nonnull final SkillType skill) {
        this.setLastUse();
        return this.node.getNode(skill.getId(), "exp").getInt();
    }

    @Override
    public void setExp(@Nonnull final SkillType skill, final int exp) {
        this.setLastUse();
        this.node.getNode(skill.getId(), "exp").setValue(exp);
    }

    @Override
    public long getLastUse() {
        return this.last_use;
    }

    private void setLastUse() {
        this.last_use = System.currentTimeMillis();
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