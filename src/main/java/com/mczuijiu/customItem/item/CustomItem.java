package com.mczuijiu.customItem.item;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.utils.ItemUtils;
import de.tr7zw.nbtapi.NBT;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomItem {
    @Getter
    private List<Action> actions;
    @Getter
    private ItemStack itemStack;
    @Getter
    private Sound sound;
    @Getter
    private boolean other;
    @Getter
    private double volume;
    @Getter
    private List<String> placeholder;
    @Getter
    private List<String> papi_message;
    @Getter
    private List<String> permission;
    @Getter
    private List<String> per_message;
    @Getter
    private int use_num;
    @Getter
    private long cool_down;
    @Getter
    private List<String> cool_message;
    @Getter
    private List<String> message;
    @Getter
    private List<String> commands;

    public static CustomItem loadCustomItem(String name, FileConfiguration config) {
        CustomItem customItem = new CustomItem();
        // 创建物品
        String itemName = config.getString("name", "");
        String itemType = config.getString("type", "");
        List<String> itemLore = config.getStringList("lore");
        boolean unbreakable = config.getBoolean("unbreakable", false);
        boolean attributes = config.getBoolean("attributes", false);
        boolean enchants = config.getBoolean("enchants", false);
        if (itemName.isEmpty() || itemType.isEmpty()) {
            return null;
        }
        itemName = ItemUtils.colorReplace(itemName);
        itemLore.replaceAll(s -> ItemUtils.colorReplace(s.replace("{useNum}",
                String.valueOf(config.getInt("use_num", -1)))));
        customItem.itemStack = new ItemStack(ItemUtils.getMaterial(itemType));
        ItemMeta meta = customItem.itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
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
        customItem.itemStack.setItemMeta(meta);
        NBT.modify(customItem.itemStack, nbt -> {
            nbt.setString(Main.getNbtKey(), name);
            nbt.setInteger(Main.getNbtKey() + ".use", 0);
            nbt.setLong(Main.getNbtKey() + ".time", 0L);
        });
        // 使用动作
        if (config.contains("actions")) {
            customItem.actions = new ArrayList<>();
            config.getStringList("actions").forEach(action -> customItem.actions.add(Action.valueOf(action)));
        }
        // 使用声音
        String soundType = config.getString("sound.type", "");
        if (!soundType.equalsIgnoreCase("")) {
            customItem.sound = Sound.valueOf(soundType);
            customItem.other = config.getBoolean("sound.other");
            customItem.volume = config.getDouble("sound.volume");
        }
        // PAPI变量条件
        customItem.placeholder = config.getStringList("placeholder");
        customItem.papi_message = config.getStringList("papi_message");
        customItem.papi_message.replaceAll(ItemUtils::colorReplace);
        // 权限条件
        customItem.permission = config.getStringList("permission");
        customItem.per_message = config.getStringList("per_message");
        customItem.per_message.replaceAll(ItemUtils::colorReplace);
        // 使用次数
        customItem.use_num = config.getInt("use_num", -1);
        // 冷却时间
        customItem.cool_down = config.getLong("cool_down", 0) * 1000;
        customItem.cool_message = config.getStringList("cool_message");
        customItem.cool_message.replaceAll(ItemUtils::colorReplace);
        // 发送消息
        customItem.message = config.getStringList("message");
        customItem.message.replaceAll(ItemUtils::colorReplace);
        // 指令集合
        customItem.commands = config.getStringList("commands");
        return customItem;
    }

}
