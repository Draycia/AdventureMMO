package me.mrdaniel.adventuremmo.catalogtypes.abilities;

import org.spongepowered.api.entity.living.player.Player;

import me.mrdaniel.adventuremmo.AdventureMMO;

public interface ActiveAbilityActions {

    ActiveAbilityActions EMPTY = new ActiveAbilityActions() {
        @Override
        public void activate(AdventureMMO mmo, Player player) {
        }

        @Override
        public void deactivate(AdventureMMO mmo, Player player) {
        }
    };

    void activate(AdventureMMO mmo, Player player);

    void deactivate(AdventureMMO mmo, Player player);
}