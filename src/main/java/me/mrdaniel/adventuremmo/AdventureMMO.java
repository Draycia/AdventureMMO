package me.mrdaniel.adventuremmo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import me.mrdaniel.adventuremmo.catalogtypes.abilities.Ability;
import me.mrdaniel.adventuremmo.catalogtypes.abilities.AbilityRegistryModule;
import me.mrdaniel.adventuremmo.catalogtypes.settings.Setting;
import me.mrdaniel.adventuremmo.catalogtypes.settings.SettingRegistryModule;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillType;
import me.mrdaniel.adventuremmo.catalogtypes.skills.SkillTypeRegistryModule;
import me.mrdaniel.adventuremmo.catalogtypes.tools.ToolType;
import me.mrdaniel.adventuremmo.catalogtypes.tools.ToolTypeRegistryModule;
import me.mrdaniel.adventuremmo.commands.CommandSettings;
import me.mrdaniel.adventuremmo.commands.CommandSkill;
import me.mrdaniel.adventuremmo.commands.CommandTop;
import me.mrdaniel.adventuremmo.data.manipulators.ImmutableMMOData;
import me.mrdaniel.adventuremmo.data.manipulators.ImmutableSettingsData;
import me.mrdaniel.adventuremmo.data.manipulators.MMOData;
import me.mrdaniel.adventuremmo.data.manipulators.MMODataBuilder;
import me.mrdaniel.adventuremmo.data.manipulators.SettingsData;
import me.mrdaniel.adventuremmo.data.manipulators.SettingsDataBuilder;
import me.mrdaniel.adventuremmo.io.Config;
import me.mrdaniel.adventuremmo.io.HoconPlayerDatabase;
import me.mrdaniel.adventuremmo.io.ItemDatabase;
import me.mrdaniel.adventuremmo.io.PlayerDatabase;
import me.mrdaniel.adventuremmo.listeners.AbilityListener;
import me.mrdaniel.adventuremmo.listeners.ClientListener;
import me.mrdaniel.adventuremmo.listeners.EXPListener;
import me.mrdaniel.adventuremmo.listeners.WorldListener;
import me.mrdaniel.adventuremmo.managers.MenuManager;
import me.mrdaniel.adventuremmo.managers.TopManager;
import me.mrdaniel.adventuremmo.utils.ChoiceMaps;

@Plugin(id = "adventuremmo",
	name = "AdventureMMO",
	version = "1.7.0",
	description = "A light-weight plugin that adds skills with all sorts of fun game mechanics to your server.",
	authors = {"Daniel12321"})
public class AdventureMMO {

	private final Game game;
	private final Logger logger;
	private final Path configdir;
	private final PluginContainer container;

	private Config config;
	private PlayerDatabase playerdata;
	private ItemDatabase itemdata;
	private MenuManager menus;
	private TopManager tops;
	private ChoiceMaps choices;

	private UserStorageService users;

	@Inject
	public AdventureMMO(final Game game, @ConfigDir(sharedRoot = false) final Path path, final PluginContainer container) {
		this.game = game;
		this.logger = LoggerFactory.getLogger("AdventureMMO");
		this.configdir = path;
		this.container = container;

		if (!Files.exists(path)) {
			try { Files.createDirectory(path); }
			catch (final IOException exc) { this.logger.error("Failed to create main config directory: {}", exc); }
		}
	}

	@Listener
	public void onPreInit(@Nullable final GamePreInitializationEvent e) {
		this.logger.info("Registering custom data...");

		this.game.getDataManager().register(MMOData.class, ImmutableMMOData.class, new MMODataBuilder());
		this.game.getDataManager().register(SettingsData.class, ImmutableSettingsData.class, new SettingsDataBuilder());

		this.game.getRegistry().registerModule(SkillType.class, new SkillTypeRegistryModule());
		this.game.getRegistry().registerModule(ToolType.class, new ToolTypeRegistryModule());
		this.game.getRegistry().registerModule(Ability.class, new AbilityRegistryModule());
		this.game.getRegistry().registerModule(Setting.class, new SettingRegistryModule());

		this.logger.info("Registered custom data successfully.");
	}

	@Listener
	public void onInit(@Nullable final GameInitializationEvent e) {
		this.logger.info("Loading plugin...");

		this.config = new Config(this, this.configdir.resolve("config.conf"));
		this.playerdata = new HoconPlayerDatabase(this, this.configdir.resolve("playerdata"));
		this.itemdata = new ItemDatabase(this, this.configdir.resolve("itemdata.conf"));
		this.menus = new MenuManager(this);
		this.tops = new TopManager(this, this.configdir.resolve("tops.conf"));
		this.choices = new ChoiceMaps();

		CommandSpec skill = CommandSpec.builder().description(Text.of(TextColors.BLUE, "AdventureMMO | Skills Command"))
				.arguments(GenericArguments.optionalWeak(GenericArguments.choices(Text.of("skill"), this.choices.getSkills())))
				.executor(new CommandSkill(this)).permission("mmo.skills").build();
		this.game.getCommandManager().register(this, skill, "skill", "skills", "mmoskill", "mmoskills");

		CommandSpec top = CommandSpec.builder().description(Text.of(TextColors.BLUE, "AdventureMMO | Top Command"))
				.arguments(GenericArguments.optional(GenericArguments.choices(Text.of("skill"), this.choices.getSkills())))
				.executor(new CommandTop(this)).permission("mmo.top").build();
		this.game.getCommandManager().register(this, top, "mmotop", "skilltop");

		CommandSpec settings = CommandSpec.builder().description(Text.of(TextColors.BLUE, "AdventureMMO | Settings Command"))
				.executor(new CommandSettings(this)).permission("mmo.settings").build();
		this.game.getCommandManager().register(this, settings, "setting", "settings", "mmosetting", "mmosettings");

		this.game.getEventManager().registerListeners(this, new ClientListener(this));
		this.game.getEventManager().registerListeners(this, new EXPListener(this));
		this.game.getEventManager().registerListeners(this, new AbilityListener(this));
		this.game.getEventManager().registerListeners(this, new WorldListener());

		this.logger.info("Loaded plugin successfully.");
	}

	@Listener
	public void onPostInit(@Nullable final GamePostInitializationEvent e) {
		this.users = this.game.getServiceManager().provide(UserStorageService.class).get();
	}

	@Listener
	public void onServiceChange(final ChangeServiceProviderEvent e) {
		if (e.getNewProvider() instanceof UserStorageService) { this.users = (UserStorageService) e.getNewProvider(); }
	}

	@Listener
	public void onReload(@Nullable final GameReloadEvent e) {
		this.logger.info("Reloading...");

		this.game.getEventManager().unregisterPluginListeners(this);
		this.game.getScheduler().getScheduledTasks(this).forEach(task -> task.cancel());
		this.game.getCommandManager().getOwnedBy(this).forEach(this.game.getCommandManager()::removeMapping);

		this.onInit(null);
		this.onPostInit(null);

		this.game.getServer().getOnlinePlayers().forEach(p -> this.playerdata.load(p.getUniqueId()));

		this.logger.info("Reloaded successfully.");
	}

	@Nonnull public Game getGame() { return this.game; }
	@Nonnull public Logger getLogger() { return this.logger; }
	@Nonnull public PluginContainer getContainer() { return this.container; }

	@Nonnull public Config getConfig() { return this.config; }
	@Nonnull public PlayerDatabase getPlayerDatabase() { return this.playerdata; }
	@Nonnull public ItemDatabase getItemDatabase() { return this.itemdata; }
	@Nonnull public MenuManager getMenus() { return this.menus; }
	@Nonnull public TopManager getTops() { return this.tops; }
	@Nonnull public ChoiceMaps getChoices() { return this.choices; }

	@Nonnull public UserStorageService getUsers() { return this.users; }
}