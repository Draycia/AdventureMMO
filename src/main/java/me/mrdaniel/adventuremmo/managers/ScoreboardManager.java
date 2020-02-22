package me.mrdaniel.adventuremmo.managers;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.MMOObject;
import me.mrdaniel.adventuremmo.io.playerdata.PlayerData;

public class ScoreboardManager extends MMOObject {

    private final Map<UUID, Task> tasks;

    public ScoreboardManager(@Nonnull final AdventureMMO mmo) {
        super(mmo);

        this.tasks = Maps.newHashMap();
    }

    public void setTemp(@Nonnull final Player player, @Nonnull final Text title,
            @Nonnull final Multimap<Integer, Text> lines) {
        this.unload(player);

        this.set(player, title, lines);

        this.tasks.put(player.getUniqueId(),
                Task.builder().delayTicks(300).execute(() -> this.unload(player)).submit(super.getMMO()));
    }

    public void setRepeating(@Nonnull final Player player, @Nonnull final Text title,
            @Nonnull final Function<PlayerData, Multimap<Integer, Text>> function) {
        this.unload(player);

        this.tasks.put(player.getUniqueId(), Task.builder().delayTicks(0).intervalTicks(100).execute(() -> {
            this.set(player, title, function.apply(super.getMMO().getPlayerDatabase().get(player.getUniqueId())));
        }).submit(super.getMMO()));
    }

    private void set(@Nonnull final Player player, @Nonnull final Text title, @Nonnull final Multimap<Integer, Text> lines) {
        Scoreboard board = Scoreboard.builder().build();
        // Objective obj = Objective.builder().name("MMO_" + (p.getName().length() > 12
        // ? p.getName().substring(0, 12) :
        // p.getName())).criterion(Criteria.DUMMY).displayName(title).build();
        Objective obj = Objective.builder().name("mmo_board").criterion(Criteria.DUMMY).displayName(title).build();
        lines.asMap().forEach((line, txts) -> txts.forEach(txt -> obj.getOrCreateScore(txt).setScore(line)));

        board.addObjective(obj);
        player.setScoreboard(board);
        board.updateDisplaySlot(obj, DisplaySlots.SIDEBAR);
    }

    public void unload(@Nonnull final Player player) {
        Optional.ofNullable(this.tasks.get(player.getUniqueId())).ifPresent(task -> {
            task.cancel();
            this.tasks.remove(player.getUniqueId());
        });
        player.getScoreboard().clearSlot(DisplaySlots.SIDEBAR);
    }
}