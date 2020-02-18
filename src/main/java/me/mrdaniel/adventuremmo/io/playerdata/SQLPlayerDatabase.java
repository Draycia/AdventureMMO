package me.mrdaniel.adventuremmo.io.playerdata;

import me.mrdaniel.adventuremmo.AdventureMMO;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SQLPlayerDatabase implements PlayerDatabase {

    private AdventureMMO plugin;
    private ConcurrentHashMap<UUID, SQLPlayerData> players;

    public SQLPlayerDatabase(@Nonnull final AdventureMMO mmo, @Nonnull final Path path) {
        if (!Files.exists(path)) {
            try {
                mmo.getContainer().getAsset("storage.db").get().copyToFile(path);
            } catch (final IOException exc) {
                mmo.getLogger().error("Failed to create database file from asset: {}", exc);
            }
        }

        this.players = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void unload(@Nonnull final UUID uuid) {
        this.players.remove(uuid);
    }

    @Override
    public synchronized void unloadAll() {
        this.players.values().forEach(SQLPlayerData::save);
        this.players.clear();
    }

    @Override
    public PlayerData get(UUID uuid) {
        // TODO: Check if player data is loaded, if it isn't, load it
        return this.players.get(uuid);
    }

    @Override
    public Optional<PlayerData> getOffline(UUID uuid) {
        // TODO: Implement
        return Optional.empty();
    }

    @Override
    public AdventureMMO getPlugin() {
        return plugin;
    }
}
