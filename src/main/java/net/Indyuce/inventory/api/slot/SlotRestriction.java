package net.Indyuce.inventory.api.slot;

import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.inventory.InventoryHandler;

public abstract class SlotRestriction {
	private final LineConfig config;

	/**
	 * Used to register item application restrictions with stats.
	 * 
	 * @param config
	 */
	public SlotRestriction(LineConfig config) {
		this.config = config;
	}

	public LineConfig getConfig() {
		return config;
	}

	/**
	 * Called when the player tries to equip an item in a specific slot.
	 *
	 * @param provider Information about the player trying to equip an item
	 * @param slot     The slot the item is being equipped in
	 * @param item     The item being equipped
	 * @return If the item can be equipped in that custom slot
	 */
	public abstract boolean isVerified(InventoryHandler provider, CustomSlot slot, NBTItem item);
}
