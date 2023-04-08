package com.mczuijiu.customItem.listener;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.item.CustomItem;
import com.mczuijiu.customItem.manager.ItemManager;
import com.mczuijiu.customItem.utils.ItemUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class PlayerUseItemListener implements Listener {

    private final ItemManager itemManager = Main.getItemManager();
    private final String prefix = Main.getMainConfig().getString("useItemPrefix");

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!Objects.equals(event.getHand(), EquipmentSlot.HAND) || !event.hasItem() || event.getItem() == null || event.getItem().getItemMeta() == null) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();
        CustomItem customItem = itemManager.getCustomItem(itemStack);
        if (customItem == null) {
            if (Main.getMainConfig().getBoolean("checkLoreEnable")) {
                String[] split = Main.getMainConfig().getString("useNumLore").split("\\{num}");
                String num = null;
                int loreIndex = -1;
                ItemMeta meta = itemStack.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null || lore.isEmpty()) {
                    return;
                }
                for (int i = 0; i < lore.size(); i++) {
                    if (lore.get(i).startsWith(split[0])) {
                        num = lore.get(i);
                        loreIndex = i;
                    }
                }
                if (num != null) {
                    num = num.replace(split[0], "").replace(split[1], "").replaceAll(" ", "");
                    if (ItemUtils.isNumeric(num)){
                        int useNum = Integer.parseInt(num) - 1;
                        if (useNum == 0) {
                            player.getInventory().setItemInMainHand(null);
                        } else {
                            lore.set(loreIndex, lore.get(loreIndex).replace(useNum+1+"", useNum+""));
                            meta.setLore(lore);
                            itemStack.setItemMeta(meta);
                            player.getInventory().setItemInMainHand(itemStack);
                        }
                    }
                    event.setCancelled(true);
                }
            }
            return;
        }
        event.setCancelled(true);
        if (Main.isPapiEnable() && !customItem.getPlaceholder().isEmpty()) {
            if (!itemManager.contentPlaceholder(player, customItem.getPlaceholder())) {
                if (!customItem.getPapi_message().isEmpty()) {
                    player.sendMessage(prefix + customItem.getPapi_message());
                }
                return;
            }
        }

        if (!customItem.getPermission().isEmpty()) {
            if (!player.hasPermission(customItem.getPermission())) {
                if (!customItem.getPer_message().isEmpty()) {
                    player.sendMessage(prefix + customItem.getPer_message());
                }
                return;
            }
        }

        NBTItem nbt = new NBTItem(itemStack);
        if (nbt.hasTag(Main.getNbtKey())) {
            long time = nbt.getLong(Main.getNbtKey() + ".time");
            if (System.currentTimeMillis() - time < customItem.getCool_down()) {
                player.sendMessage(prefix + customItem.getCool_message());
                return;
            }

            int useNum = nbt.getInteger(Main.getNbtKey() + ".use");
            if (useNum != -1) {
                useNum++;
                if (useNum == customItem.getUse_num()) {
                    if (itemStack.getAmount() > 1) {
                        itemStack.setAmount(itemStack.getAmount() - 1);
                        useNum = 0;
                        nbt.setLong(Main.getNbtKey() + ".time", System.currentTimeMillis());
                        nbt.setInteger(Main.getNbtKey() + ".use", useNum);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                } else {
                    nbt.setLong(Main.getNbtKey() + ".time", System.currentTimeMillis());
                    nbt.setInteger(Main.getNbtKey() + ".use", useNum);
                    player.getInventory().setItemInMainHand(itemStack);
                }
            }
        }

        if (customItem.getSound() != null) {
            if (customItem.isOther()) {
                player.playSound(player.getLocation(), customItem.getSound(), (float) customItem.getVolume(), 1.0f);
            } else {
                player.playSound(player, customItem.getSound(), (float) customItem.getVolume(), 1.0f);
            }
        }

        if (!customItem.getMessage().isEmpty()) {
            player.sendMessage(prefix + customItem.getMessage());
        }

        if (!customItem.getAnnounce().isEmpty()) {
            Bukkit.broadcastMessage(prefix + customItem.getAnnounce());
        }

        if (!customItem.getCommands().isEmpty()) {

        }

    }

}
