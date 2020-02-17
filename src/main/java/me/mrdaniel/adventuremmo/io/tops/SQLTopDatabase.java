package me.mrdaniel.adventuremmo.io.tops;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import org.spongepowered.api.util.Tuple;

import java.util.Map;

public class SQLTopDatabase implements TopDatabase {

    public SQLTopDatabase(AdventureMMO mmo) {

    }

    @Override
    public void update(String player, SkillType skill, int level) {

    }

    @Override
    public Map<Integer, Tuple<String, Integer>> getTop(SkillType skill) {
        return null;
    }

}
