package me.mrdaniel.adventuremmo.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public abstract class PlayerCommand implements CommandExecutor {

    @Override
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, "This commands is for players only."));
            return CommandResult.success();
        }

        Player player = (Player) src;

        this.execute(player, args);

        return CommandResult.success();
    }

    public abstract void execute(Player player, CommandContext args) throws CommandException;
}