package me.mrdaniel.adventuremmo.commands;

import javax.annotation.Nonnull;

import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;

public class CommandTop extends PlayerCommand {

    private final AdventureMMO mmo;

    public CommandTop(@Nonnull final AdventureMMO mmo) {
        this.mmo = mmo;
    }

    @Override
    public void execute(final Player player, final CommandContext args) {
        this.mmo.getMenus().sendSkillTop(player, args.<SkillType>getOne("skill").orElse(null));
    }
}