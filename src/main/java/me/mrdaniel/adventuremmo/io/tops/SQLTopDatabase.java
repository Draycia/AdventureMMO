package me.mrdaniel.adventuremmo.io.tops;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Tuple;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLTopDatabase implements TopDatabase {

    private AdventureMMO mmo;
    private DataSource dataSource;

    private UserStorageService storage = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();

    public SQLTopDatabase(AdventureMMO mmo, DataSource dataSource) {
        this.mmo = mmo;
        this.dataSource = dataSource;
    }

    @Override
    public void update(String player, SkillType skill, int level) {

    }

    @Override
    public Map<Integer, Tuple<String, Integer>> getTop(SkillType skill) {
        Map<Integer, Tuple<String, Integer>> top = new HashMap<>();

        int i = 1;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM skill_" + skill.getId() + " ORDER BY level, experience DESC LIMIT 10")) {
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    UUID uuid = new UUID(resultSet.getLong(2), resultSet.getLong(1));
                    String name = storage.get(uuid).get().getName();

                    top.put(i++, new Tuple<>(name, resultSet.getInt(3)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return top;
    }

}
