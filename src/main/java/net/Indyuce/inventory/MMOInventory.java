package net.Indyuce.inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

import net.Indyuce.inventory.comp.MMOItemsUniqueRestriction;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.Indyuce.inventory.api.ConfigFile;
import net.Indyuce.inventory.api.inventory.SimpleInventoryHandler;
import net.Indyuce.inventory.command.MMOInventoryCommand;
import net.Indyuce.inventory.command.MMOInventoryCompletion;
import net.Indyuce.inventory.comp.MMOItemsCompatibility;
import net.Indyuce.inventory.comp.MMOItemsLevelRestriction;
import net.Indyuce.inventory.comp.MMOItemsTypeRestriction;
import net.Indyuce.inventory.listener.DeathDrops;
import net.Indyuce.inventory.listener.GuiListener;
import net.Indyuce.inventory.listener.InventoryButtonListener;
import net.Indyuce.inventory.listener.NoCustomInventory;
import net.Indyuce.inventory.listener.PlayerListener;
import net.Indyuce.inventory.listener.ResourcePack;
import net.Indyuce.inventory.listener.SaveOnLeave;
import net.Indyuce.inventory.manager.DataManager;
import net.Indyuce.inventory.manager.SlotManager;
import net.Indyuce.inventory.sql.SQLManager;
import net.Indyuce.inventory.version.ServerVersion;
import net.Indyuce.inventory.version.wrapper.VersionWrapper;
import net.Indyuce.inventory.version.wrapper.VersionWrapper_Reflection;

public class MMOInventory extends JavaPlugin implements Listener {
	public static MMOInventory plugin;

	private final DataManager dataManager = new DataManager();
	private final SlotManager slotManager = new SlotManager();
	private final SQLManager sqlManager = new SQLManager();

	private ServerVersion version;
	private VersionWrapper versionWrapper;
	private ConfigFile language;

	/*
	 * cached config options
	 */
	public int inventorySlots;

	public void onLoad() {
		plugin = this;

		if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
			slotManager.registerRestriction(config -> new MMOItemsTypeRestriction(config), "mmoitemstype", "mmoitemtype", "mitype");
			slotManager.registerRestriction(config -> new MMOItemsLevelRestriction(config), "mmoitemslevel", "mmoitemlevel", "milevel");
			slotManager.registerRestriction(config -> new MMOItemsUniqueRestriction(config), "unique");
		}
	}

	public void onEnable() {

		version = new ServerVersion(Bukkit.getServer().getClass());
		try {
			getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
			versionWrapper = (VersionWrapper) Class.forName("net.Indyuce.inventory.version.wrapper.VersionWrapper_" + version.toString().substring(1))
					.newInstance();
		} catch (Exception e) {
			getLogger().log(Level.INFO, "Your server version is handled via reflection");
			versionWrapper = new VersionWrapper_Reflection();
		}

		saveDefaultConfig();
		saveDefaultFile("language");
		saveDefaultFile("items");
		reload();

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new GuiListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		sqlManager.load();
		
		if (getConfig().getBoolean("resource-pack.enabled"))
			Bukkit.getServer().getPluginManager().registerEvents(new ResourcePack(getConfig().getConfigurationSection("resource-pack")), this);

		if (getConfig().getBoolean("no-custom-inventory")) {
			dataManager.setInventoryProvider(player -> new SimpleInventoryHandler(player));
			Bukkit.getPluginManager().registerEvents(new NoCustomInventory(), this);
		}

		else {

			if (getConfig().getBoolean("save-on-leave"))
				Bukkit.getPluginManager().registerEvents(new SaveOnLeave(), this);

			if (getConfig().getBoolean("inventory-button.enabled"))
				Bukkit.getPluginManager().registerEvents(new InventoryButtonListener(getConfig().getConfigurationSection("inventory-button")), this);

			if (getConfig().getBoolean("drop-on-death"))
				Bukkit.getServer().getPluginManager().registerEvents(new DeathDrops(), this);
		}

		getCommand("mmoinventory").setExecutor(new MMOInventoryCommand());
		getCommand("mmoinventory").setTabCompleter(new MMOInventoryCompletion());

		// /reload friendly
		Bukkit.getOnlinePlayers().forEach(player -> dataManager.setupData(player));

		if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
			new MMOItemsCompatibility();
			getLogger().log(Level.INFO, "Hooked onto MMOItems");
		}
	}

	public void onDisable() {
		dataManager.save();
	}

	public void reload() {
		reloadConfig();
		language = new ConfigFile("language");
		slotManager.reload();

		try {
			inventorySlots = getConfig().getInt("inventory-slots");
			Validate.isTrue(inventorySlots > 0 && inventorySlots < 55, "Number must be greater than 9 and lower than 54");
			Validate.isTrue(inventorySlots % 9 == 0, "Number must be a multiple of 9");

		} catch (IllegalArgumentException exception) {
			inventorySlots = 36;
			getLogger().log(Level.WARNING, "Invalid inventory slot number: " + exception.getMessage());
		}
	}

	public VersionWrapper getVersionWrapper() {
		return versionWrapper;
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public SlotManager getSlotManager() {
		return slotManager;
	}
	
	public SQLManager getSQLManager() {
		return sqlManager;
	}

	public ServerVersion getVersion() {
		return version;
	}

	public String getTranslation(String path) {
		return ChatColor.translateAlternateColorCodes('&', language.getConfig().getString(path));
	}

	private void saveDefaultFile(String path) {
		try {
			File file = new File(getDataFolder(), path + ".yml");
			if (!file.exists())
				Files.copy(getResource("default/" + path + ".yml"), file.getAbsoluteFile().toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
