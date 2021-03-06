package net.Indyuce.inventory.manager.sql;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.inventory.CustomInventoryHandler;
import net.Indyuce.inventory.inventory.InventoryHandler;
import net.Indyuce.inventory.manager.DataManager;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SQLDataManager extends DataManager {
    private final SQLManager sqlManager = new SQLManager();

    public SQLDataManager() {
        sqlManager.setup(MMOInventory.plugin.getConfig());
    }

    @Override
    public void save() {
        for (InventoryHandler handler : getLoaded())
            if (handler instanceof CustomInventoryHandler)
                save((CustomInventoryHandler) handler);
        sqlManager.close();
    }

    @Override
    public void save(CustomInventoryHandler data) {
        sqlManager.save(data.getUniqueId().toString(), data.getMapped().entrySet());
    }

    @Override
    public void load(CustomInventoryHandler data) {
        SQLUserdata userData = sqlManager.getUserData(data.getUniqueId().toString());
        for (Map.Entry<Integer, ItemStack> entry : userData.get())
            data.getMapped().put(entry.getKey(), entry.getValue());
    }
}
