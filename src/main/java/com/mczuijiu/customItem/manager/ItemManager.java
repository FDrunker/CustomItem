package com.mczuijiu.customItem.manager;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.item.CustomItem;
import com.mczuijiu.customItem.utils.ItemUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
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
        loadAllFile(files);
        plugin.getLogger().info("配置文件初始化完成");
    }

    private void loadAllFile(File[] files) {
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.exists()) continue;
            if (file.isDirectory()) {
                loadAllFile(file.listFiles());
                continue;
            }
            FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(file);
            CustomItem item = new CustomItem();
            String fileName = file.getName().replace(".yml", "");
            if (item.initCustomItem(fileName, itemConfig)) {
                itemMap.put(fileName, item);
                plugin.getLogger().info("初始化 " + file.getName() + " 成功");
            } else {
                plugin.getLogger().warning("配置文件 " + file.getName() + " 读取失败, 请检查文件!");
            }
        }
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

    public ItemStack computeUseNum(CustomItem customItem, NBTItem nbt) {
        int useNum = nbt.getInteger(Main.getNbtKey() + ".use");
        ItemStack itemStack = null;
        if (useNum != -1) {
            useNum++;
            if (useNum == customItem.getUse_num()) {
                if (nbt.getItem().getAmount() == 1) {
                    return null;
                }
                nbt.setInteger(Main.getNbtKey() + ".use", 0);
                itemStack = nbt.getItem();
                itemStack.setAmount(itemStack.getAmount() - 1);
                return itemStack;
            }
            nbt.setInteger(Main.getNbtKey() + ".use", useNum);
            itemStack = nbt.getItem();
        }
        return itemStack;
    }

    public void executeCommands(List<String> commands, Player player) {
        for(String cmd: commands) {
            if (!cmd.contains("->")) {
                continue;
            }
            String[] split = cmd.split("->");
            if (split[0].equalsIgnoreCase("cmd")) {
                Bukkit.dispatchCommand(player, split[1].replace("[player]", player.getName()));
                continue;
            }
            if (split[0].equalsIgnoreCase("servercmd")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), split[1].replace("[player]", player.getName()));
                continue;
            }
            plugin.getLogger().info(cmd + "填写格式错误, 无法执行!");
        }
    }

}
