package me.mrdaniel.adventuremmo.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.catalogtypes.abilities.Ability;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillTypes;
import me.mrdaniel.adventuremmo.data.manipulators.MMOData;
import me.mrdaniel.adventuremmo.io.playerdata.PlayerData;
import me.mrdaniel.adventuremmo.utils.MathUtils;
import me.mrdaniel.adventuremmo.utils.TextUtils;

public class MenuManager {

    private final AdventureMMO mmo;
    private final ScoreboardManager scoreboards;
    private int i;

    public MenuManager(@Nonnull final AdventureMMO mmo) {
        this.mmo = mmo;
        this.scoreboards = new ScoreboardManager(mmo);
        this.i = 0;
    }

    public void sendAdminView(@Nonnull final MessageReceiver src, @Nonnull final PlayerData data,
            @Nonnull final String name) {
        src.sendMessage(Text.EMPTY);
        src.sendMessage(this.getTitle(name, false));
        src.sendMessage(Text.of(TextColors.AQUA, "Total", TextColors.GRAY, " - ", TextColors.GREEN, "Level ",
                data.getLevels()));
        SkillTypes.VALUES.forEach(skill -> src.sendMessage(Text.of(TextColors.AQUA, skill.getName(), TextColors.GRAY,
                " - ", TextColors.GREEN, "Level ", data.getLevel(skill))));
        src.sendMessage(Text.EMPTY);
    }

    public void sendSkillList(@Nonnull final Player p) {
        PlayerData pdata = this.scoreboards.getMMO().getPlayerDatabase().get(p.getUniqueId());
        MMOData sdata = p.get(MMOData.class).orElse(new MMOData());

        if (sdata.getScoreboard()) {
            if (sdata.getScoreboardPermanent()) {
                this.scoreboards.setRepeating(p, this.getTitle("Skills", true), this::getSkillListLines);
            } else {
                this.scoreboards.setTemp(p, this.getTitle("Skills", true), this.getSkillListLines(pdata));
            }
        } else {
            p.sendMessage(Text.EMPTY);
            p.sendMessage(this.getTitle("Skills", false));
            p.sendMessage(Text.of(TextColors.AQUA, "Total", TextColors.GRAY, " - ", TextColors.GREEN, "Level ",
                    pdata.getLevels()));
            SkillTypes.VALUES.forEach(skill -> p.sendMessage(Text.builder()
                    .append(Text.of(TextColors.AQUA, skill.getName(), TextColors.GRAY, " - ", TextColors.GREEN,
                            "Level ", pdata.getLevel(skill)))
                    .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click for more info.")))
                    .onClick(TextActions.runCommand("/mmoskill " + skill.getId())).build()));
            p.sendMessage(Text.EMPTY);
        }
    }

    public void sendSkillInfo(@Nonnull final Player player, @Nonnull final SkillType skill) {
        PlayerData pdata = this.scoreboards.getMMO().getPlayerDatabase().get(player.getUniqueId());
        MMOData sdata = player.get(MMOData.class).orElse(new MMOData());

        if (sdata.getScoreboard()) {
            if (sdata.getScoreboardPermanent()) {
                this.scoreboards.setRepeating(player, this.getTitle(skill.getName(), true),
                        data -> this.getSkillInfoLines(data, skill));
            } else {
                this.scoreboards.setTemp(player, this.getTitle(skill.getName(), true), this.getSkillInfoLines(pdata, skill));
            }
        } else {
            player.sendMessage(Text.EMPTY);
            player.sendMessage(this.getTitle(skill.getName(), false));
            player.sendMessage(Text.of(TextColors.GREEN, "Level: ", pdata.getLevel(skill)));
            player.sendMessage(Text.of(TextColors.GREEN, "EXP: ", pdata.getExp(skill), " / ",
                    MathUtils.expTillNextLevel(pdata.getLevel(skill))));
            skill.getAbilities().forEach(ability -> {
                player.sendMessage(Text.EMPTY);
                player.sendMessage(this.getBoardTitle(ability.getName(), false));
                player.sendMessage(ability.getValueLine(pdata.getLevel(skill)));
            });
            player.sendMessage(Text.EMPTY);
        }
    }

    public void sendSkillTop(@Nonnull final Player player, @Nullable final SkillType type) {
        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    MMOData sdata = player.get(MMOData.class).orElse(new MMOData());
                    String title = (type == null) ? "Total Top" : (type.getName() + " Top");

                    if (sdata.getScoreboard()) {
                        if (sdata.getScoreboardPermanent()) {
                            this.scoreboards.setRepeating(player, this.getTitle(title, true), data -> this.getSkillTopLines(type));
                        } else {
                            this.scoreboards.setTemp(player, this.getTitle(title, true), this.getSkillTopLines(type));
                        }
                    } else {
                        player.sendMessage(Text.EMPTY);
                        player.sendMessage(this.getTitle(title, false));
                        this.scoreboards.getMMO().getTops().getTop(type)
                                .forEach((number, topPlayer) -> player
                                        .sendMessage(Text.of(TextColors.RED, number, ": ", TextColors.AQUA, topPlayer.getFirst(),
                                                TextColors.GRAY, " - ", TextColors.GREEN, "Level ", topPlayer.getSecond())));
                        player.sendMessage(Text.EMPTY);
                    }
                }).async().submit(mmo);
    }

    public void sendSettingsInfo(@Nonnull final Player player) {
        MMOData data = player.get(MMOData.class).orElse(new MMOData());

        player.sendMessage(Text.EMPTY);
        player.sendMessage(this.getTitle("Settings", false));

        player.sendMessage(Text.builder()
                .append(Text.of(TextColors.AQUA, "Action Bar: ", TextUtils.getValueText(data.getActionBar())))
                .onHover(TextActions.showText(TextUtils.getToggleText(data.getActionBar())))
                .onClick(TextActions.executeCallback(src -> {
                    data.setActionBar(!data.getActionBar());
                    player.offer(data);
                    this.sendSettingsInfo((Player) src);
                })).build());

        player.sendMessage(Text.builder()
                .append(Text.of(TextColors.AQUA, "Scoreboard: ", TextUtils.getValueText(data.getScoreboard())))
                .onHover(TextActions.showText(TextUtils.getToggleText(data.getScoreboard())))
                .onClick(TextActions.executeCallback(src -> {
                    data.setScoreboard(!data.getScoreboard());
                    player.offer(data);
                    this.sendSettingsInfo((Player) src);
                    this.scoreboards.unload(player);
                })).build());

        player.sendMessage(Text.builder()
                .append(Text.of(TextColors.AQUA, "Scoreboard Permanent: ",
                        TextUtils.getValueText(data.getScoreboardPermanent())))
                .onHover(TextActions.showText(TextUtils.getToggleText(data.getScoreboardPermanent())))
                .onClick(TextActions.executeCallback(src -> {
                    data.setScoreboardPermanent(!data.getScoreboardPermanent());
                    player.offer(data);
                    this.sendSettingsInfo((Player) src);
                    this.scoreboards.unload(player);
                })).build());

        player.sendMessage(Text.EMPTY);
    }

    @Nonnull
    private Multimap<Integer, Text> getSkillListLines(@Nonnull final PlayerData data) {
        Multimap<Integer, Text> lines = ArrayListMultimap.create();

        SkillTypes.VALUES.forEach(type -> lines.put(data.getLevel(type),
                Text.of(TextColors.AQUA, type.getName(), TextColors.GRAY, " - ")));
        lines.put(data.getLevels(), Text.of(TextColors.GREEN, "Total", TextColors.GRAY, " - "));

        return lines;
    }

    @Nonnull
    private Multimap<Integer, Text> getSkillInfoLines(@Nonnull final PlayerData data, @Nonnull final SkillType skill) {
        Multimap<Integer, Text> lines = ArrayListMultimap.create();
        int i = 1;

        for (Ability ability : skill.getAbilities()) {
            lines.put(i++, ability.getValueLine(data.getLevel(skill)));
            lines.put(i++, this.getBoardTitle(ability.getName(), true));
            lines.put(i++, this.getEmptyLine());
        }

        lines.put(i++, Text.of(TextColors.GREEN, "EXP: ", data.getExp(skill), " / ",
                MathUtils.expTillNextLevel(data.getLevel(skill))));
        lines.put(i, Text.of(TextColors.GREEN, "Level: ", data.getLevel(skill)));

        return lines;
    }

    @Nonnull
    private Multimap<Integer, Text> getSkillTopLines(@Nullable final SkillType type) {
        Multimap<Integer, Text> lines = ArrayListMultimap.create();

        this.scoreboards.getMMO().getTops().getTop(type).forEach(
                (number, player) -> lines.put(player.getSecond(), Text.of(TextColors.AQUA, player.getFirst())));

        return lines;
    }

    @Nonnull
    private Text getTitle(@Nonnull final String txt, final boolean small) {
        return small ? Text.of(TextColors.RED, "--=[ ", TextColors.AQUA, txt, TextColors.RED, " ]=--")
                : Text.of(TextColors.RED, "----===[ ", TextColors.AQUA, txt, TextColors.RED, " ]===---");
    }

    @Nonnull
    private Text getBoardTitle(@Nonnull final String txt, final boolean small) {
        return small ? Text.of(TextColors.RED, "-=[ ", TextColors.DARK_GREEN, txt, TextColors.RED, " ]=-")
                : Text.of(TextColors.RED, "---==[ ", TextColors.AQUA, txt, TextColors.RED, " ]==---");
    }

    @Nonnull
    private Text getEmptyLine() {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < this.i; i++) {
            str.append(" ");
        }

        if (this.i++ > 8) {
            this.i = 0;
        }

        return Text.of(str.toString());
    }

    @Nonnull
    public ScoreboardManager getScoreboardManager() {
        return this.scoreboards;
    }
}