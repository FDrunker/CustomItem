package com.mczuijiu.customItem.listener;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.item.CustomItem;
import com.mczuijiu.customItem.manager.ItemManager;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PlayerUseItemListener implements Listener {

    private final ItemManager itemManager = Main.getItemManager();
    private final String prefix = Main.getMainConfig().getString("useItemPrefix");

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!Objects.equals(event.getHand(), EquipmentSlot.HAND) || !event.hasItem() || event.getItem() == null) {
            return;
        }
        ItemStack itemStack = event.getItem();
        if (!NBT.get(itemStack, nbt -> nbt.hasTag(Main.getNbtKey()))) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        CustomItem customItem = itemManager.getCustomItem(NBT.get(itemStack, nbt -> nbt.getString(Main.getNbtKey())));

        if (Main.isPapiEnable() && !customItem.getPlaceholder().isEmpty()) {
            if (!itemManager.contentPlaceholder(player, customItem.getPlaceholder())) {
                player.sendMessage(prefix + customItem.getPapi_message());
                return;
            }
        }

        if (!customItem.getPermission().isEmpty()) {
            if (!player.hasPermission(customItem.getPermission())) {
                player.sendMessage(prefix + customItem.getPer_message());
                return;
            }
        }

        long time = NBT.get(itemStack, nbt -> nbt.getLong(Main.getNbtKey() + ".time"));
        if (System.currentTimeMillis() - time < customItem.getCool_down()) {
            player.sendMessage(prefix + customItem.getCool_message());
            return;
        }

        int use_num = NBT.get(itemStack, nbt -> nbt.getInteger(Main.getNbtKey() + ".use")) + 1;

        if (use_num >= customItem.getUse_num()) {
            if (itemStack.getAmount() > 1) {
                itemStack.setAmount(itemStack.getAmount() - 1);
                use_num = 0;
            } else {

            }
        }

        int finalUse_num = use_num;
        NBT.modify(itemStack, nbt -> {
            nbt.setInteger(Main.getNbtKey() + ".use", finalUse_num);
            nbt.setLong(Main.getNbtKey() + ".time", System.currentTimeMillis());
        });

        if (customItem.getSound() != null) {
            if (customItem.isOther()) {
                player.playSound(player.getLocation(), customItem.getSound(), (float) customItem.getVolume(), 0);
            } else {
                player.playSound(player, customItem.getSound(), (float) customItem.getVolume(), 0);
            }
        }

    }

}
