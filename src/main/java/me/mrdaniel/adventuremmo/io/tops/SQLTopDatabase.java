package me.mrdaniel.adventuremmo.io.tops;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import org.spongepowered.api.util.Tuple;

import javax.sql.DataSource;
import java.util.Map;

public class SQLTopDatabase implements TopDatabase {

    private AdventureMMO mmo;
    private DataSource dataSource;

    public SQLTopDatabase(AdventureMMO mmo, DataSource dataSource) {
        this.mmo = mmo;
        this.dataSource = dataSource;
    }

    @Override
    public void update(String player, SkillType skill, int level) {

    }

    @Override
    public Map<Integer, Tuple<String, Integer>> getTop(SkillType skill) {
        return null;
    }

}
