package net.Indyuce.inventory.version.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.version.NBTItem;
import net.Indyuce.inventory.version.ItemTag;

public class VersionWrapper_Reflection implements VersionWrapper {

    @Override
    public boolean isHelmet(Material material) {
        return material.getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    @Override
    public ItemStack getModelItem(Material material, int model) {
        return new NBTItem_Reflection(new ItemStack(material)).addTag(new ItemTag("CustomModelData", model)).toItem();
    }

    @Override
    public NBTItem getNBTItem(ItemStack item) {
        return new NBTItem_Reflection(item);
    }

    private Class<?> obc(String str) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + MMOInventory.plugin.getVersion().toString() + "." + str);
    }

    public class NBTItem_Reflection extends NBTItem {
        private net.minecraft.world.item.ItemStack nms;
        private NBTTagCompound compound;

        public NBTItem_Reflection(ItemStack item) {
            super(item);

            try {
                Class<?> craftItemStack = obc("inventory.CraftItemStack");
                nms = (net.minecraft.world.item.ItemStack) craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(craftItemStack, item);
                compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
                    | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public String getString(String path) {
            return compound.getString(path);
        }

        @Override
        public boolean hasTag(String path) {
            return compound.hasKey(path);
        }

        @Override
        public boolean getBoolean(String path) {
            return compound.getBoolean(path);
        }

        @Override
        public double getDouble(String path) {
            return compound.getDouble(path);
        }

        @Override
        public int getInteger(String path) {
            return compound.getInt(path);
        }

        @Override
        public NBTItem addTag(List<ItemTag> tags) {
            tags.forEach(tag -> {
                if (tag.getValue() instanceof Boolean)
                    compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
                else if (tag.getValue() instanceof Double)
                    compound.setDouble(tag.getPath(), (double) tag.getValue());
                else if (tag.getValue() instanceof String)
                    compound.setString(tag.getPath(), (String) tag.getValue());
                else if (tag.getValue() instanceof Integer)
                    compound.setInt(tag.getPath(), (int) tag.getValue());
            });
            return this;
        }

        @Override
        public NBTItem removeTag(String... paths) {
            for (String path : paths)
                compound.remove(path);
            return this;
        }

        @Override
        public Set<String> getTags() {
            return compound.getKeys();
        }

        @Override
        public ItemStack toItem() {
            try {
                nms.setTag(compound);
                Class<?> craftItemStack = obc("inventory.CraftItemStack");
                return (ItemStack) craftItemStack.getMethod("asBukkitCopy", nms.getClass()).invoke(craftItemStack, nms);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                    | SecurityException | ClassNotFoundException exception) {
                exception.printStackTrace();
                return null;
            }
        }
    }
}
