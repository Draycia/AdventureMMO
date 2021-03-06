package me.mrdaniel.adventuremmo.io.items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.collect.Maps;

import me.mrdaniel.adventuremmo.AdventureMMO;
import me.mrdaniel.adventuremmo.MMOObject;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillTypes;
import me.mrdaniel.adventuremmo.catalogtypes.tools.ToolType;
import me.mrdaniel.adventuremmo.catalogtypes.tools.ToolTypes;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class HoconItemDatabase extends MMOObject implements ItemDatabase {

    private final Map<BlockType, BlockData> blocks;
    private final Map<ItemType, ToolData> tools;

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final CommentedConfigurationNode node;

    public HoconItemDatabase(@Nonnull final AdventureMMO mmo, @Nonnull final Path path) {
        super(mmo);

        this.blocks = Maps.newHashMap();
        this.tools = Maps.newHashMap();

        this.loader = HoconConfigurationLoader.builder().setPath(path).build();

        if (!Files.exists(path)) {
            try {
                mmo.getContainer().getAsset("itemdata.conf").get().copyToFile(path);
            } catch (final IOException exc) {
                mmo.getLogger().error("Failed to create itemdata file: {}", exc);
            }
        }
        this.node = this.load();

        this.node.getNode("blocks").getChildrenMap().forEach((ido, value) -> {
            String id = (String) ido;
            Optional<BlockType> type = super.getGame().getRegistry().getType(BlockType.class, id);
            Optional<BlockData> data = BlockData.deserialize(value.getString());
            if (type.isPresent()) {
                if (data.isPresent()) {
                    if (SkillTypes.VALUES.contains(data.get().getSkill())) {
                        this.blocks.put(type.get(), data.get());
                    }
                } else {
                    super.getLogger().warn("Invalid exp format for block id {}, skipping!", id);
                }
            } else {
                super.getLogger().warn("Failed to find block id {}, skipping!", id);
            }
        });

        this.node.getNode("tools").getChildrenMap().forEach((ido, value) -> {
            String id = (String) ido;
            Optional<ItemType> type = super.getGame().getRegistry().getType(ItemType.class, id);
            Optional<ToolData> data = ToolData.deserialize(value.getString());
            if (type.isPresent()) {
                if (data.isPresent()) {
                    this.tools.put(type.get(), data.get());
                } else {
                    super.getLogger().warn("Invalid tooltype format for tool id {}, skipping!", id);
                }
            } else {
                super.getLogger().warn("Failed to find item id {}, skipping!", id);
            }
        });
    }

    private CommentedConfigurationNode load() {
        try {
            return this.loader.load();
        } catch (final IOException exc) {
            super.getMMO().getLogger().error("Failed to load itemdata file: {}", exc);
            return this.loader.createEmptyNode();
        }
    }

    private void save() {
        try {
            this.loader.save(this.node);
        } catch (final IOException exc) {
            super.getLogger().error("Failed to save itemdata file: {}", exc);
        }
    }

    @Override
    @Nonnull
    public Optional<BlockData> getData(@Nonnull final BlockType type) {
        return Optional.ofNullable(this.blocks.get(type));
    }

    @Override
    @Nonnull
    public Optional<ToolData> getData(@Nonnull final ItemType type) {
        return Optional.ofNullable(this.tools.get(type));
    }

    @Override
    @Nonnull
    public Optional<ToolData> getData(@Nullable final ItemStack item) {
        if (item == null) {
            return Optional.of(new ToolData(ToolTypes.HAND));
        }
        return Optional.ofNullable(this.tools.get(item.getType()));
    }

    @Override
    public void set(@Nonnull final ItemType item, @Nullable final ToolType tool) {
        ToolData data = new ToolData(tool);
        this.tools.put(item, data);
        this.node.getNode("tools", item.getId()).setValue(data.serialize());
        this.save();
    }

    @Override
    public void set(@Nonnull final BlockType block, @Nonnull final SkillType skill, final int exp) {
        BlockData data = new BlockData(skill, exp);
        this.blocks.put(block, data);
        this.node.getNode("blocks", block.getId()).setValue(data.serialize());
        this.save();
    }

    @Override
    public void remove(@Nonnull final ItemType item) {
        this.tools.remove(item);
        this.node.getNode("tools").removeChild(item.getId());
        this.save();
    }

    @Override
    public void remove(@Nonnull final BlockType block) {
        this.blocks.remove(block);
        this.node.getNode("blocks").removeChild(block.getId());
        this.save();
    }
}