package com.mczuijiu.customItem.manager;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.item.CustomItem;
import com.mczuijiu.customItem.utils.ItemUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemManager {

    private final Main plugin;
    private final Map<String, CustomItem> itemMap = new HashMap<>();
    private final String prefix = ItemUtils.colorReplace(Main.getMainConfig().getString("useItemPrefix", ""));


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

    /**
     * 读取数组中所有文件
     * 如果file为文件夹则递归
     *
     * @param files 文件夹下所有子文件
     */
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
            String fileName = file.getName().replace(".yml", "");
            CustomItem item = CustomItem.loadCustomItem(fileName, itemConfig);
            if (item != null) {
                itemMap.put(fileName, item);
                plugin.getLogger().info("初始化 " + file.getName() + " 成功");
            } else {
                plugin.getLogger().warning("配置文件 " + file.getName() + " 读取失败, 请检查文件!");
            }
        }
    }

    /**
     * 获取所有item文件名
     *
     * @return 文件名Set
     */
    public Set<String> getAllItemName() {
        return itemMap.keySet();
    }

    /**
     * 以文件名获取CustomItem
     *
     * @param fileName 文件名
     * @return 自定义物品
     */
    public CustomItem getCustomItem(String fileName) {
        return itemMap.get(fileName);
    }

    /**
     * 以物品名获取CustomItem
     *
     * @param itemStack 物品
     * @return 自定义物品
     */
    public CustomItem getCustomItem(ItemStack itemStack) {
        if (NBT.get(itemStack, nbt -> nbt.hasTag(Main.getNbtKey()))) {
            return itemMap.get(NBT.get(itemStack, nbt -> nbt.getString(Main.getNbtKey())));
        } else if (Main.getMainConfig().getBoolean("checkNameEnable")) {
            for (String fileName : itemMap.keySet()) {
                if (itemStack.getItemMeta() != null && itemStack.getItemMeta().getDisplayName().contains(fileName)) {
                    return itemMap.get(fileName);
                }
            }
        }
        return null;
    }

    /**
     * 判断PAPI变量条件
     *
     * @param player 玩家
     * @param placeholder papi条件语句
     * @return 是否满足条件
     */
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
        if (placeholder.contains(">=")) {
            String[] split = placeholder.split(">=");
            String str = PlaceholderAPI.setPlaceholders(player, split[0]);
            if (ItemUtils.isNumeric(split[1])) {
                return Integer.parseInt(str) >= Integer.parseInt(split[1]);
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
        if (placeholder.contains("<=")) {
            String[] split = placeholder.split("<=");
            String str = PlaceholderAPI.setPlaceholders(player, split[0]);
            if (ItemUtils.isNumeric(split[1])) {
                return Integer.parseInt(str) <= Integer.parseInt(split[1]);
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

    /**
     * 计算使用次数
     * 如果使用次数已达上限, 但物品amount只有1时
     * 则返回null
     *
     * @param customItem 自定义物品
     * @param nbt 物品nbt
     * @return 物品
     */
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
                ItemUtils.replaceLoreString(itemStack, "{useNum}", String.valueOf(customItem.getUse_num()));
                itemStack.setAmount(itemStack.getAmount() - 1);
                return itemStack;
            }
            nbt.setInteger(Main.getNbtKey() + ".use", useNum);
            itemStack = nbt.getItem();
            ItemUtils.replaceLoreString(itemStack, "{useNum}", String.valueOf(customItem.getUse_num() - useNum));
        }
        return itemStack;
    }

    /**
     * 给玩家发送消息
     *
     * @param player 玩家
     * @param message 消息
     */
    public void sendMessage(Player player, String message) {
        if (!message.contains("->")) {
            return;
        }
        String[] split = message.split("->");
        String msg = PlaceholderAPI.setPlaceholders(player, split[1]);
        if (split[0].equalsIgnoreCase("chat")) {
            player.sendMessage(prefix + msg);
            return;
        }
        if (split[0].equalsIgnoreCase("actionbar")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
            return;
        }
        if (split[0].equalsIgnoreCase("title")) {
            player.sendTitle(msg, "", 1, 2, 1);
            return;
        }
        if (split[0].equalsIgnoreCase("bossbar")) {
            Bukkit.createBossBar(msg, BarColor.PURPLE, BarStyle.SEGMENTED_10);
            return;
        }
        if (split[0].equalsIgnoreCase("announce")) {
            Bukkit.broadcastMessage(msg);
            return;
        }
        plugin.getLogger().info(message + "格式填写错误, 无法发送!");
    }

    /**
     * 执行集合中的所有指令
     *
     * @param player 玩家
     * @param command 指令集合
     */
    public void executeCommands(Player player, String command) {
        if (!command.contains("->")) {
            return;
        }
        String[] split = command.split("->");
        if (split[0].equalsIgnoreCase("cmd")) {
            Bukkit.dispatchCommand(player, split[1].replace("[player]", player.getName()));
            return;
        }
        if (split[0].equalsIgnoreCase("servercmd")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), split[1].replace("[player]", player.getName()));
            return;
        }
        plugin.getLogger().info(command + "填写格式错误, 无法执行!");
    }

}
