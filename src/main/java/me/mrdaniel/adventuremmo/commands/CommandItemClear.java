package me.mrdaniel.adventuremmo.commands;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import me.mrdaniel.adventuremmo.AdventureMMO;

public class CommandItemClear extends PlayerCommand {

    private final AdventureMMO mmo;

    public CommandItemClear(@Nonnull final AdventureMMO mmo) {
        this.mmo = mmo;
    }

    @Override
    public void execute(final Player player, final CommandContext args) {
        Optional<ItemStack> hand = player.getItemInHand(HandTypes.MAIN_HAND);

        if (!hand.isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "You must be holding an item to use this command"));
            return;
        }

        ItemType item = hand.get().getType();

        this.mmo.getItemDatabase().remove(item);
        this.mmo.getMessages().sendItemClear(player, item);
    }
}