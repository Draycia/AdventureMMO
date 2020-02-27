package me.mrdaniel.adventuremmo.io.playerdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import me.mrdaniel.adventuremmo.event.PlayerDataEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import me.mrdaniel.adventuremmo.AdventureMMO;

public class HoconPlayerDatabase implements PlayerDatabase {

    private final AdventureMMO plugin;
    private final Path path;
    private final Map<UUID, HoconPlayerData> players;

    public HoconPlayerDatabase(@Nonnull final AdventureMMO plugin, @Nonnull final Path path) {
        this.plugin = plugin;
        this.path = path;
        this.players = new ConcurrentHashMap<>();

        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (final IOException exc) {
                plugin.getLogger().error("Failed to create main playerdata directory: {}", exc);
            }
        }

        Task.builder().async().delay(30, TimeUnit.SECONDS).interval(30, TimeUnit.SECONDS).execute(() -> {
            this.players.values().forEach(HoconPlayerData::save);
            this.players.entrySet().stream()
                    .filter(e -> e.getValue().getLastUse() < System.currentTimeMillis() - 180000).map(Map.Entry::getKey)
                    .collect(Collectors.toList()).forEach(this.players::remove);
        }).submit(plugin);
    }

    @Override
    public synchronized void unload(@Nonnull final UUID uuid) {
        Optional.ofNullable(this.players.get(uuid)).ifPresent(data -> {
            data.save();
            Sponge.getEventManager().post(new PlayerDataEvent.Unload(data));
            this.players.remove(uuid);
        });
    }

    @Override
    public synchronized void unloadAll() {
        this.players.values().forEach(data -> {
            data.save();
            Sponge.getEventManager().post(new PlayerDataEvent.Unload(data));
        });
        this.players.clear();
    }

    @Override
    @Nonnull
    public synchronized PlayerData get(@Nonnull final UUID uuid) {
        HoconPlayerData data = this.players.get(uuid);

        if (data == null) {
            data = new HoconPlayerData(this.path.resolve(uuid.toString() + ".conf"), uuid);
            this.players.put(uuid, data);

            Sponge.getEventManager().post(new PlayerDataEvent.Load(data));
        }

        return data;
    }

    @Override
    @Nonnull
    public Optional<PlayerData> getOffline(@Nonnull final UUID uuid) {
        Path path = this.path.resolve(uuid.toString() + ".conf");

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        return Optional.of(new HoconPlayerData(path, uuid));
    }

    @Override
    public AdventureMMO getPlugin() {
        return plugin;
    }
}