package com.mczuijiu.customItem.manager;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.item.CustomItem;
import com.mczuijiu.customItem.utils.ItemUtils;
import de.tr7zw.nbtapi.NBT;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemManager {

    private final Main plugin;
    private final Map<String, CustomItem> itemMap = new HashMap<>();

    public ItemManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadItems() {
        this.itemMap.clear();
        plugin.getLogger().info("初始化配置文件中...");
        File folder = new File(plugin.getDataFolder(), "items");
        if (!folder.exists() || !folder.isDirectory()) {
            plugin.saveResource("items/example.yml", false);
        }
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].exists()) continue;
            if (files[i].isDirectory()) continue;
            FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(files[i]);
            CustomItem item = new CustomItem();
            String fileName = files[i].getName().replace(".yml", "");
            if (item.initCustomItem(fileName, itemConfig)) {
                itemMap.put(fileName, item);
                plugin.getLogger().info("初始化 " + files[i].getName() + " 成功");
            } else {
                plugin.getLogger().warning("配置文件 " + files[i].getName() + " 读取失败, 请检查文件!");
            }
        }
        plugin.getLogger().info("配置文件初始化完成");
    }

    public Set<String> getAllItemName() {
        return itemMap.keySet();
    }

    public CustomItem getCustomItem(String fileName) {
        return itemMap.get(fileName);
    }

    public CustomItem getCustomItem(ItemStack itemStack) {
        if (NBT.get(itemStack, nbt -> nbt.hasTag(Main.getNbtKey()))) {
            return itemMap.get(NBT.get(itemStack, nbt -> nbt.getString(Main.getNbtKey())));
        } else if (Main.getMainConfig().getBoolean("checkNameEnable")) {
            for (String fileName : itemMap.keySet()) {
                if (itemStack.getItemMeta().getDisplayName().contains(fileName)) {
                    return itemMap.get(fileName);
                }
            }
        }
        return null;
    }

    public boolean contentPlaceholder(Player player, String placeholder) {
        if (placeholder.contains("=")) {
            String[] split = placeholder.split("=");
            String str = PlaceholderAPI.setPlaceholders(player, split[0]);
            if (ItemUtils.isNumeric(split[1])) {
                return Integer.parseInt(str) == Integer.parseInt(split[1]);
            }
            return str.equalsIgnoreCase(split[1]);
        }
        if (placeholder.contains(">")) {
            String[] split = placeholder.split(">");
            String str = PlaceholderAPI.setPlaceholders(player, split[0]);
            if (ItemUtils.isNumeric(split[1])) {
                return Integer.parseInt(str) > Integer.parseInt(split[1]);
            }
            return false;
        }
        if (placeholder.contains("<")) {
            String[] split = placeholder.split("<");
            String str = PlaceholderAPI.setPlaceholders(player, split[0]);
            if (ItemUtils.isNumeric(split[1])) {
                return Integer.parseInt(str) < Integer.parseInt(split[1]);
            }
            return false;
        }
        if (placeholder.contains("!=")) {
            String[] split = placeholder.split("!=");
            String str = PlaceholderAPI.setPlaceholders(player, split[0]);
            if (ItemUtils.isNumeric(split[1])) {
                return Integer.parseInt(str) != Integer.parseInt(split[1]);
            }
            return !str.equalsIgnoreCase(split[1]);
        }
        return false;
    }

}
