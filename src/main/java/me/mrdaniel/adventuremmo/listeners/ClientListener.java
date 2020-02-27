package me.mrdaniel.adventuremmo.listeners;

import javax.annotation.Nonnull;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.MMOObject;
import me.mrdaniel.adventuremmo.utils.ItemUtils;

public class ClientListener extends MMOObject {

    public ClientListener(@Nonnull final AdventureMMO mmo) {
        super(mmo);
    }

    @Listener
    public void onQuit(final ClientConnectionEvent.Disconnect e) {
        // TODO: Unload on a timer after disconnect
        //super.getMMO().getPlayerDatabase().unload(e.getTargetEntity().getUniqueId());
        super.getMMO().getMenus().getScoreboardManager().unload(e.getTargetEntity());
        ItemUtils.restoreSuperTool(e.getTargetEntity(), super.getMMO().getContainer());
    }

    @Listener
    public void onJoin(final ClientConnectionEvent.Join event) {
        // Loads the player's data if it isn't already loaded
        super.getMMO().getPlayerDatabase().get(event.getTargetEntity().getUniqueId());
    }
}