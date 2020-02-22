package me.mrdaniel.adventuremmo.commands;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.utils.ServerUtils;

public class CommandBlockSet extends PlayerCommand {

    private final AdventureMMO mmo;

    public CommandBlockSet(@Nonnull final AdventureMMO mmo) {
        this.mmo = mmo;
    }

    @Override
    public void execute(final Player player, final CommandContext args) {
        Optional<Location<World>> loc = ServerUtils.getFirstBlock(player);

        if (!loc.isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "You must be looking at a block."));
            return;
        }

        BlockType block = loc.get().getBlockType();
        SkillType skill = args.<SkillType>getOne("skill").get();
        int exp = args.<Integer>getOne("exp").get();

        this.mmo.getItemDatabase().set(block, skill, exp);
        this.mmo.getMessages().sendBlockSet(player, block, skill, exp);
    }
}