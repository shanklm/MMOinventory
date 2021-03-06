package net.Indyuce.inventory.manager;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.inventory.CustomInventoryHandler;
import net.Indyuce.inventory.inventory.InventoryHandler;
import net.Indyuce.inventory.util.ConfigFile;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class YamlDataManager extends DataManager {

    @Override
    public void save() {
        for (InventoryHandler handler : getLoaded())
            if (handler instanceof CustomInventoryHandler)
                save((CustomInventoryHandler) handler);
    }

    @Override
    public void save(CustomInventoryHandler data) {
        ConfigFile config = new ConfigFile(MMOInventory.plugin, "/userdata", data.getUniqueId().toString());

        // Important: CLEAR current data
        config.getConfig().set("inventory", null);

        try {
            for (int index : data.getFilledSlotKeys()) {
                ItemStack item = data.getItem(index);
                config.getConfig().set("inventory." + index, isAir(item) ? null : item);
            }
            config.save();
        } catch (Exception exception) {
            MMOInventory.plugin.getLogger().log(Level.SEVERE, "Could not save inventory of " + data.getPlayer().getName() + ": " + exception.getMessage());
        }
    }

    @Override
    public void load(CustomInventoryHandler data) {
        FileConfiguration config = new ConfigFile("/userdata", data.getUniqueId().toString()).getConfig();

        if (config.contains("inventory"))
            for (String key : config.getConfigurationSection("inventory").getKeys(false))
                try {
                    int index = Integer.parseInt(key);
                    data.getMapped().put(index, config.getItemStack("inventory." + index));
                } catch (IllegalArgumentException exception) {
                    MMOInventory.plugin.getLogger().log(Level.SEVERE, "Could not read inventory item indexed " + key + " of " + data.getPlayer().getName() + ": " + exception.getMessage());
                }
    }

    private boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
