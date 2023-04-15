package com.mczuijiu.customItem.commands;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.manager.ItemManager;
import com.mczuijiu.customItem.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ItemCommands implements TabExecutor {

    private final ItemManager itemManager;
    private final String prefix;

    public ItemCommands() {
        this.itemManager = Main.getItemManager();
        this.prefix = ItemUtils.colorReplace(Main.getMainConfig().getString("prefix", ""));
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("customitem.admin") || !sender.isOp()) {
            sender.sendMessage(prefix + "§c你没有权限执行该指令");
            return true;
        }
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "help" -> {
                    helpCommand(sender);
                    return true;
                }
                case "reload" -> {
                    reloadCommand(sender);
                    return true;
                }
                case "give" -> {
                    giveCommand(sender, args);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("customitem.admin") || !sender.isOp()) return null;
        LinkedList<String> tips = new LinkedList<>();
        if (args.length == 1) {
            List<String> firstArgList = Arrays.asList("help", "reload", "give");
            if (args[0].isEmpty()) {
                tips.addAll(firstArgList);
                return tips;
            } else {
                firstArgList.forEach(firstArg -> {
                    if (firstArg.toLowerCase().startsWith(args[0].toLowerCase())) tips.add(firstArg);
                });
            }
            return tips;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                if (args[1].isEmpty()) {
                    Bukkit.getOnlinePlayers().forEach(player -> tips.add(player.getName()));
                } else {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            tips.add(player.getName());
                        }
                    });
                }
                return tips;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give") && !args[1].isEmpty()) {
                if (args[2].isEmpty()) {
                    tips.addAll(itemManager.getAllItemName());
                } else {
                    itemManager.getAllItemName().forEach(itemName -> {
                        if (itemName.toLowerCase().startsWith(args[2].toLowerCase())) {
                            tips.add(itemName);
                        }
                    });
                }
                return tips;
            }
        }
        return tips;
    }

    private void helpCommand(CommandSender sender) {
        sender.sendMessage("§a---------- §6[CustomItem Help] §a----------");
        sender.sendMessage("§b/customitem give {player} {item} ->§a给予玩家自定义物品");
    }

    private void reloadCommand(CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Main.getInstance().reloadConfig();
            Main.getItemManager().loadItems();
            sender.sendMessage(prefix + "§a重载配置文件完成");
        });
    }

    private void giveCommand(CommandSender sender, String[] args) {
        if (!args[1].isEmpty()) {
            Player player = Bukkit.getPlayer(args[1]);
            if (Objects.nonNull(player)) {
                if (!args[2].isEmpty()) {
                    if (itemManager.getAllItemName().contains(args[2])) {
                        player.getInventory().addItem(itemManager.getCustomItem(args[2]).getItemStack());
                        sender.sendMessage(prefix + "§a给予玩家 " + args[2] + " 成功");
                    } else {
                        sender.sendMessage(prefix + "§4未找到 " + args[2] + " 物品, 请检查配置文件");
                    }
                } else {
                    sender.sendMessage(prefix + "§4未知指令");
                }
            } else {
                sender.sendMessage(prefix + "该玩家不在线");
            }
        } else {
            sender.sendMessage(prefix + "§4未知指令");
        }
    }
}
