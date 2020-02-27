package me.mrdaniel.adventuremmo.catalogtypes.skills;

public class SkillData {

    private int level;
    private int experience;

    public SkillData(int level, int experience) {
        this.level = level;
        this.experience = experience;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
