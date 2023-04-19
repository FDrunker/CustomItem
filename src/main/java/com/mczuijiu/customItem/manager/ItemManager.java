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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                String displayName = itemMeta.getDisplayName();
                for (Map.Entry<String, CustomItem> entry : itemMap.entrySet()) {
                    if (displayName.contains(entry.getKey())) {
                        return entry.getValue();
                    }
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
        Pattern pattern = Pattern.compile("([^><=!]+)([><=!]+)([^><=!]+)");
        Matcher matcher = pattern.matcher(placeholder);
        if (!matcher.matches()) {
            return false;
        }
        String left = PlaceholderAPI.setPlaceholders(player, matcher.group(1));
        String operator = matcher.group(2);
        String right = matcher.group(3);
        if (ItemUtils.isNumeric(left) && ItemUtils.isNumeric(right)) {
            int leftNum = Integer.parseInt(left);
            int rightNum = Integer.parseInt(right);
            return switch (operator) {
                case ">" -> leftNum > rightNum;
                case "<" -> leftNum < rightNum;
                case ">=" -> leftNum >= rightNum;
                case "<=" -> leftNum <= rightNum;
                case "=" -> leftNum == rightNum;
                case "!=" -> leftNum != rightNum;
                default -> false;
            };
        } else {
            return switch (operator) {
                case "=" -> left.equalsIgnoreCase(right);
                case "!=" -> !left.equalsIgnoreCase(right);
                default -> false;
            };
        }
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
        if (useNum == -1) {
            return null;
        }
        useNum++;
        if (useNum != customItem.getUse_num()) {
            nbt.setInteger(Main.getNbtKey() + ".use", useNum);
            return nbt.getItem();
        }
        ItemStack itemStack = nbt.getItem();
        int newAmount = itemStack.getAmount() - 1;
        if (newAmount == 0) {
            return null;
        }
        itemStack.setAmount(newAmount);
        nbt.setInteger(Main.getNbtKey() + ".use", 0);
        return itemStack;
    }

    /**
     * 给玩家发送消息
     *
     * @param player 玩家
     * @param message 消息
     */
    public void sendMessage(Player player, String message) {
        Pattern pattern = Pattern.compile("(chat|actionbar|announce)->(.+)");
        Matcher matcher = pattern.matcher(message);
        if (!matcher.matches()) {
            plugin.getLogger().info(message + " 格式填写错误，无法发送!");
            return;
        }
        String messageType = matcher.group(1);
        String msg = PlaceholderAPI.setPlaceholders(player, matcher.group(2));
        switch (messageType.toLowerCase()) {
            case "chat" -> player.sendMessage(prefix + msg);
            case "actionbar" -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
            case "announce" -> Bukkit.broadcastMessage(msg);
            default -> plugin.getLogger().info(message + " 格式填写错误，无法发送!");
        }
    }

    /**
     * 执行集合中的所有指令
     *
     * @param player 玩家
     * @param command 指令集合
     */
    public void executeCommands(Player player, String command) {
        Pattern pattern = Pattern.compile("(cmd|servercmd)->(.+)");
        Matcher matcher = pattern.matcher(command);
        if (!matcher.matches()) {
            plugin.getLogger().info(command + " 填写格式错误，无法执行!");
            return;
        }
        String commandType = matcher.group(1);
        String cmd = matcher.group(2).replace("[player]", player.getName());
        switch (commandType.toLowerCase()) {
            case "cmd" -> Bukkit.dispatchCommand(player, cmd);
            case "servercmd" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            default -> plugin.getLogger().info(command + " 填写格式错误，无法执行!");
        }
    }

}
