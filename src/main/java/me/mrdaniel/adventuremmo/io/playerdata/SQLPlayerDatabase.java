package me.mrdaniel.adventuremmo.io.playerdata;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SQLPlayerDatabase implements PlayerDatabase {

    private AdventureMMO plugin;
    private ConcurrentHashMap<UUID, SQLPlayerData> players;
    private String uri;
    private SqlService sqlService;

    public SQLPlayerDatabase(@Nonnull final AdventureMMO mmo, @Nonnull final Path path) {
        this.plugin = mmo;

        Path dbFile = path.resolve("storage.db");

        if (!Files.exists(dbFile)) {
            try {
                Files.createFile(dbFile);
            } catch (final IOException exc) {
                mmo.getLogger().error("Failed to create database file from asset: {}", exc);
                return;
            }
        }

        Optional<SqlService> sql = Sponge.getServiceManager().provide(SqlService.class);

        if (!sql.isPresent()) {
            mmo.getLogger().error("Failed to initialize database file");
            return;
        } else {
            this.uri = "jdbc:h2:" + dbFile.toAbsolutePath().toString();
            this.sqlService = sql.get();

            setupTables();
        }

        this.players = new ConcurrentHashMap<>();
    }

    public DataSource getDataSource() {
        try {
            return sqlService.getDataSource(uri);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public synchronized void unload(@Nonnull final UUID uuid) {
        this.players.remove(uuid);
        // TODO: Save?
    }

    @Override
    public synchronized void unloadAll() {
        System.out.println("Unloading and saving player data!");
        this.players.values().forEach(SQLPlayerData::save);
        this.players.clear();
    }

    private CompletableFuture<PlayerData> loadDataAsync(UUID uuid) {
        // TODO: stub

        return CompletableFuture.supplyAsync(() -> {
            try {
                SQLPlayerData playerData = new SQLPlayerData(uuid, getDataSource());

                try (Connection connection = getDataSource().getConnection()) {
                    for (SkillType skillType : SkillTypes.VALUES) {
                        try (PreparedStatement statement = connection.prepareStatement("SELECT level, experience FROM skill_" + skillType.getId() + " WHERE id_lsig = ? AND id_msig = ?;")) {
                            statement.setLong(1, uuid.getLeastSignificantBits());
                            statement.setLong(2, uuid.getMostSignificantBits());

                            try (ResultSet resultSet = statement.executeQuery()) {
                                try {
                                    int level = 0;
                                    int experience = 0;

                                    if (resultSet.next()) {
                                        level = resultSet.getInt(1);
                                        experience = resultSet.getInt(2);
                                    }

                                    playerData.setLevel(skillType, level);
                                    playerData.setExp(skillType, experience);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                players.put(uuid, playerData);

                return playerData;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    private void initialUserSave(PlayerData data) {
        try (Connection connection = getDataSource().getConnection()) {
            for (SkillType skillType : SkillTypes.VALUES) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO skill_" + skillType.getId() + " VALUES (0, 0, ?, ?)")) {
                    statement.setLong(1, data.getPlayerUUID().getLeastSignificantBits());
                    statement.setLong(2, data.getPlayerUUID().getMostSignificantBits());

                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData get(UUID uuid) {
        PlayerData data = this.players.get(uuid);

        if (data != null) {
            return data;
        }

        // future
        loadDataAsync(uuid);

        return new DummySqlPlayerData(uuid, getDataSource());
    }

    @Override
    public Optional<PlayerData> getOffline(UUID uuid) {
        // TODO: Implement
        // TODO: CompletableFuture<Optional<PlayerData>> getOffline(UUID uuid)
        return Optional.empty();
    }

    @Override
    public AdventureMMO getPlugin() {
        return plugin;
    }

    private void setupTables() {
        try (Connection connection = getDataSource().getConnection()) {
            for (SkillType skillType : SkillTypes.VALUES) {
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS skill_" + skillType.getId() + " (id_lsig bigint NOT NULL, id_msig bigint NOT NULL, level integer NOT NULL, experience integer NOT NULL, PRIMARY KEY (id_lsig, id_msig));")) {
                    statement.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
