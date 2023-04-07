package com.mczuijiu.customItem.item;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.utils.ItemUtils;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CustomItem {
    private ItemStack itemStack;
    private Sound sound;
    private boolean other;
    private double volume;
    private String placeholder;
    private String papi_message;
    private String permission;
    private String per_message;
    private int use_num;
    private long cool_down;
    private String cool_message;
    private String message;
    private String announce;
    private List<String> commands;

    public boolean initCustomItem(String name, FileConfiguration config) {
        String itemName = config.getString("name", "");
        String itemType = config.getString("type", "");
        List<String> itemLore = config.getStringList("lore");
        boolean unbreakable = config.getBoolean("unbreakable", false);
        boolean attributes = config.getBoolean("attributes", false);
        boolean enchants = config.getBoolean("enchants", false);
        if (itemName.isEmpty() || itemType.isEmpty()) {
            return false;
        }
        itemName = itemName.replaceAll("&", "§");
        itemLore.replaceAll(s -> s.replaceAll("&", "§"));
        this.itemStack = new ItemStack(ItemUtils.getMaterial(itemType));
        ItemMeta meta = this.itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(itemName);
        meta.setLore(itemLore);
        if (unbreakable) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        if (attributes) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        if (enchants) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        this.itemStack.setItemMeta(meta);
        NBT.modify(itemStack, nbt -> {
            nbt.setString(Main.getNbtKey(), name);
            nbt.setInteger(Main.getNbtKey() + ".use", 0);
            nbt.setLong(Main.getNbtKey() + ".time", 0L);
        });
        String soundType = config.getString("sound.type", "");
        if (!soundType.equalsIgnoreCase("")) {
            this.sound = Sound.valueOf(soundType);
            this.other = config.getBoolean("sound.other");
            this.volume = config.getDouble("sound.volume");
        }
        this.placeholder = config.getString("placeholder", "");
        this.papi_message = config.getString("papi_message", "");
        this.permission = config.getString("permission", "");
        this.per_message = config.getString("per_message", "");
        this.use_num = config.getInt("use_num", -1);
        this.cool_down = config.getLong("cool_down", 0) * 1000;
        this.cool_message = config.getString("cool_message", "");
        this.message = config.getString("message", "").replaceAll("&", "§");
        this.announce = config.getString("announce", "").replaceAll("&", "§");
        this.commands = config.getStringList("commands");
        return true;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Sound getSound() {
        return sound;
    }

    public boolean isOther() {
        return other;
    }

    public double getVolume() {
        return volume;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getPapi_message() {
        return papi_message;
    }

    public String getPermission() {
        return permission;
    }

    public String getPer_message() {
        return per_message;
    }

    public int getUse_num() {
        return use_num;
    }

    public long getCool_down() {
        return cool_down;
    }

    public String getCool_message() {
        return cool_message;
    }

    public String getMessage() {
        return message;
    }

    public String getAnnounce() {
        return announce;
    }

    public List<String> getCommands() {
        return commands;
    }
}
