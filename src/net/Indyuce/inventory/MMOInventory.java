package net.Indyuce.inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.Indyuce.inventory.api.ConfigFile;
import net.Indyuce.inventory.command.RPGInventoryCommand;
import net.Indyuce.inventory.command.RPGInventoryCompletion;
import net.Indyuce.inventory.comp.MMOItemsCompatibility;
import net.Indyuce.inventory.listener.DeathDrops;
import net.Indyuce.inventory.listener.GuiListener;
import net.Indyuce.inventory.listener.PlayerListener;
import net.Indyuce.inventory.listener.ResourcePack;
import net.Indyuce.inventory.manager.DataManager;
import net.Indyuce.inventory.manager.SlotManager;
import net.Indyuce.inventory.version.ServerVersion;
import net.Indyuce.inventory.version.wrapper.VersionWrapper;

public class MMOInventory extends JavaPlugin implements Listener {
	public static MMOInventory plugin;

	private final DataManager dataManager = new DataManager();
	private final SlotManager slotManager = new SlotManager();

	private VersionWrapper versionWrapper;
	private ConfigFile language;

	/*
	 * cached config options
	 */
	public int inventorySlots;

	public MMOInventory() {
		plugin = this;
	}

	public void onEnable() {

		ServerVersion version = new ServerVersion(Bukkit.getServer().getClass());
		try {
			getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
			versionWrapper = (VersionWrapper) Class.forName("net.Indyuce.inventory.version.wrapper.VersionWrapper_" + version.toString().substring(1)).newInstance();
		} catch (Exception e) {
			getLogger().log(Level.INFO, "Your server version is not compatible.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		saveDefaultConfig();
		saveDefaultFile("language");
		saveDefaultFile("items");
		reload();

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new GuiListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		if (getConfig().getBoolean("drop-on-death"))
			Bukkit.getServer().getPluginManager().registerEvents(new DeathDrops(), this);

		if (getConfig().getBoolean("resource-pack.enabled"))
			Bukkit.getServer().getPluginManager().registerEvents(new ResourcePack(getConfig().getConfigurationSection("resource-pack")), this);

		if (getConfig().getBoolean("save-on-leave"))
			Bukkit.getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void a(PlayerQuitEvent event) {
					dataManager.getInventory(event.getPlayer()).save();
					dataManager.unload(event.getPlayer());
				}
			}, this);

		getCommand("mmoinventory").setExecutor(new RPGInventoryCommand());
		getCommand("mmoinventory").setTabCompleter(new RPGInventoryCompletion());

		Bukkit.getOnlinePlayers().forEach(player -> dataManager.loadInventory(player));

		if (Bukkit.getPluginManager().getPlugin("MMOItems") != null) {
			new MMOItemsCompatibility();
			getLogger().log(Level.INFO, "Hooked onto MMOItems");
		}
	}

	public void onDisable() {
		dataManager.getLoaded().forEach(data -> data.save());
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

	public String getTranslation(String path) {
		return ChatColor.translateAlternateColorCodes('&', language.getConfig().getString(path));
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