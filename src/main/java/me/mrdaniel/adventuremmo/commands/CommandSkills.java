package me.mrdaniel.adventuremmo.commands;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;

public class CommandSkills extends PlayerCommand {

    private final AdventureMMO mmo;

    public CommandSkills(@Nonnull final AdventureMMO mmo) {
        this.mmo = mmo;
    }

    @Override
    public void execute(final Player player, final CommandContext args) {
        Optional<SkillType> skill = args.getOne("skill");

        if (!skill.isPresent()) {
            this.mmo.getMenus().sendSkillList(player);
        } else {
            this.mmo.getMenus().sendSkillInfo(player, skill.get());
        }
    }
}