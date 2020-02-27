package me.mrdaniel.adventuremmo.managers;

import javax.annotation.Nonnull;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.MMOObject;
import me.mrdaniel.adventuremmo.catalogtypes.abilities.Ability;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.catalogtypes.tools.ToolType;
import me.mrdaniel.adventuremmo.data.manipulators.MMOData;
import me.mrdaniel.adventuremmo.utils.TextUtils;
import ninja.leaping.configurate.ConfigurationNode;

public class MessageManager extends MMOObject {

    private final int delay_seconds;

    private final Text dodge;
    private final Text roll;
    private final Text disarm;

    private final Text reload;

    private final String set;
    private final String setitem;
    private final String clearitem;
    private final String setblock;
    private final String clearblock;

    private final String levelup;
    private final String ability_recharge;
    private final String ability_activate;
    private final String ability_end;

    public MessageManager(@Nonnull final AdventureMMO mmo, @Nonnull final ConfigurationNode node) {
        super(mmo);

        this.delay_seconds = node.getNode("seconds_between_messages").getInt(5);

        String prefix = node.getNode("prefix").getString("&8[&9MMO&8]");
        if (!prefix.equals("")) {
            prefix += " ";
        }

        this.dodge = TextUtils.toText(prefix + node.getNode("dodge").getString(""));
        this.roll = TextUtils.toText(prefix + node.getNode("roll").getString(""));
        this.disarm = TextUtils.toText(prefix + node.getNode("disarm").getString(""));

        this.reload = TextUtils.toText(prefix + "&cReloaded successfully.");

        this.set = prefix + "&aYou set %player%'s %skill% level to %level%";
        this.setitem = prefix + "&aYou allowed %item% to be used as a(n) %tool%.";
        this.clearitem = prefix + "&aYou removed %item% from the tool list.";
        this.setblock = prefix + "&aYou set %block% to give %exp% exp to the %skill% skill when broken.";
        this.clearblock = prefix + "&aYou removed %block% from the block list.";

        this.levelup = prefix + node.getNode("levelup").getString("");
        this.ability_recharge = prefix + node.getNode("ability_recharge").getString("");
        this.ability_activate = prefix + node.getNode("ability_activate").getString("");
        this.ability_end = prefix + node.getNode("ability_end").getString("");
    }

    public void sendDodge(@Nonnull final Player player) {
        this.send(player, this.dodge, player.get(MMOData.class).orElse(new MMOData()));
    }

    public void sendRoll(@Nonnull final Player player) {
        this.send(player, this.roll, player.get(MMOData.class).orElse(new MMOData()));
    }

    public void sendDisarm(@Nonnull final Player player) {
        this.send(player, this.disarm, player.get(MMOData.class).orElse(new MMOData()));
    }

    public void sendLevelUp(@Nonnull final Player player, @Nonnull final SkillType skill, final int level) {
        this.send(player,
                TextUtils.toText(
                        this.levelup.replace("%skill%", skill.getName()).replace("%level%", String.valueOf(level))),
                player.get(MMOData.class).orElse(new MMOData()));
    }

    public void sendAbilityRecharge(@Nonnull final Player player, final int seconds) {
        this.sendDelayed(player, TextUtils.toText(this.ability_recharge.replace("%seconds%", String.valueOf(seconds))),
                player.get(MMOData.class).orElse(new MMOData()));
    }

    public void sendAbilityActivate(@Nonnull final Player player, @Nonnull final Ability ability) {
        this.send(player, TextUtils.toText(this.ability_activate.replace("%ability%", ability.getName())),
                player.get(MMOData.class).orElse(new MMOData()));
    }

    public void sendAbilityEnd(@Nonnull final Player player, @Nonnull final Ability ability) {
        this.send(player, TextUtils.toText(this.ability_end.replace("%ability%", ability.getName())),
                player.get(MMOData.class).orElse(new MMOData()));
    }

    public void sendReload(@Nonnull final CommandSource src) {
        src.sendMessage(this.reload);
    }

    public void sendSet(@Nonnull final CommandSource src, @Nonnull final String player, @Nonnull final SkillType skill,
            final int level) {
        src.sendMessage(TextUtils.toText(this.set.replace("%player%", player).replace("%skill%", skill.getName())
                .replace("%level%", String.valueOf(level))));
    }

    public void sendItemSet(@Nonnull final Player player, @Nonnull final ItemType item, @Nonnull final ToolType tool) {
        player.sendMessage(
                TextUtils.toText(this.setitem.replace("%item%", item.getName()).replace("%tool%", tool.getName())));
    }

    public void sendItemClear(@Nonnull final Player player, @Nonnull final ItemType item) {
        player.sendMessage(TextUtils.toText(this.clearitem.replace("%item%", item.getName())));
    }

    public void sendBlockSet(@Nonnull final Player player, @Nonnull final BlockType block, @Nonnull final SkillType skill,
            final int exp) {
        player.sendMessage(TextUtils.toText(this.setblock.replace("%block%", block.getName())
                .replace("%skill%", skill.getName()).replace("%exp%", String.valueOf(exp))));
    }

    public void sendBlockClear(@Nonnull final Player player, @Nonnull final BlockType block) {
        player.sendMessage(TextUtils.toText(this.clearblock.replace("%block%", block.getName())));
    }

    private void sendDelayed(@Nonnull final Player player, @Nonnull final Text txt, @Nonnull final MMOData data) {
        if (!data.isDelayActive("message_delay")) {
            this.send(player, txt, data);
        }
    }

    private void send(@Nonnull final Player player, @Nonnull final Text txt, @Nonnull final MMOData data) {
        player.sendMessage(data.getActionBar() ? ChatTypes.ACTION_BAR : ChatTypes.CHAT, txt);

        data.setDelay("message_delay", System.currentTimeMillis() + (this.delay_seconds * 1000));
        player.offer(data);
    }
}