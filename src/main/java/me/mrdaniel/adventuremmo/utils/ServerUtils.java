package me.mrdaniel.adventuremmo.utils;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ServerUtils {

	@Nonnull
	public static Optional<Location<World>> getFirstBlock(@Nonnull final Player p) {

		for (BlockRayHit<World> worldBlockRayHit : BlockRay.from(p).distanceLimit(50)) {
			Location<World> loc = worldBlockRayHit.getLocation();

			if (loc.getBlockType() != BlockTypes.AIR) {
				return Optional.of(loc);
			}
		}

		return Optional.empty();
	}
}