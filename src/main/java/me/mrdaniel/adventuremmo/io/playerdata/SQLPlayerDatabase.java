package me.mrdaniel.adventuremmo.io.playerdata;

import me.mrdaniel.adventuremmo.AdventureMMO;

import java.util.Optional;
import java.util.UUID;

public class SQLPlayerDatabase implements PlayerDatabase {

    public SQLPlayerDatabase(AdventureMMO mmo) {

    }

    @Override
    public void unload(UUID uuid) {

    }

    @Override
    public void unloadAll() {

    }

    @Override
    public PlayerData get(UUID uuid) {
        return null;
    }

    @Override
    public Optional<PlayerData> getOffline(UUID uuid) {
        return Optional.empty();
    }

}
