package net.Indyuce.inventory.comp;

import net.Indyuce.inventory.api.LineConfig;
import net.Indyuce.inventory.api.NBTItem;
import net.Indyuce.inventory.api.inventory.InventoryHandler;
import net.Indyuce.inventory.api.slot.CustomSlot;
import net.Indyuce.inventory.api.slot.SlotRestriction;
import net.Indyuce.mmoitems.api.Type;

public class MMOItemsTypeRestriction extends SlotRestriction {

	/**
	 * Forced to save the MMOItems type as a string and not a type instance
	 * because the TypeManager has not been initialized yet... which is fine
	 * because we don't need to get the Type instance for our checks
	 */
	private final String id;

	public MMOItemsTypeRestriction(LineConfig config) {
		super(config);

		config.validate("type");
		id = config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_");
	}

	@Override
	public boolean isVerified(InventoryHandler provider, CustomSlot slot, NBTItem item) {
		Type type = Type.get(item.getString("MMOITEMS_ITEM_TYPE"));
		return type != null && id.equals(type.getId());
	}

	public String getTypeId() {
		return id;
	}
}
