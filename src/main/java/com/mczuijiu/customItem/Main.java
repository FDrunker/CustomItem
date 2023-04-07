package com.mczuijiu.customItem;

import com.mczuijiu.customItem.commands.ItemCommands;
import com.mczuijiu.customItem.listener.PlayerUseItemListener;
import com.mczuijiu.customItem.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private static ItemManager itemManager;
    private final static String nbtKey = "customitem";
    private static boolean papiEnable;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        saveResource("items/example.yml", false);
        itemManager = new ItemManager(instance);
        itemManager.loadItems();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papiEnable = true;
            instance.getLogger().info("检测到PlaceholderAPI插件, 支持变量判断");
        } else {
            papiEnable = false;
            instance.getLogger().info("未检测到PlaceholderAPI插件, 部分功能失效");
        }
        Bukkit.getPluginCommand("customitem").setExecutor(new ItemCommands());
        Bukkit.getPluginManager().registerEvents(new PlayerUseItemListener(), this);
    }

    @Override
    public void onDisable() {

    }

    public static Main getInstance() {
        return instance;
    }

    public static ItemManager getItemManager() {
        return itemManager;
    }

    public static String getNbtKey() {
        return nbtKey;
    }

    public static FileConfiguration getMainConfig() {
        return instance.getConfig();
    }

    public static boolean isPapiEnable() {
        return papiEnable;
    }
}
