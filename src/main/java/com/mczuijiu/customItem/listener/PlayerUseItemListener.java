package com.mczuijiu.customItem.listener;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.item.CustomItem;
import com.mczuijiu.customItem.manager.ItemManager;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PlayerUseItemListener implements Listener {

    private final ItemManager itemManager = Main.getItemManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!Objects.equals(event.getHand(), EquipmentSlot.HAND)
                || !event.hasItem()
                || event.getItem() == null
                || event.getItem().getItemMeta() == null) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();
        CustomItem customItem = itemManager.getCustomItem(itemStack);

        if (customItem == null) {
            return;
        }
        event.setCancelled(true);

        boolean canUse = canUseItem(customItem, event.getAction(), player);
        if (!canUse) {
            return;
        }

        // NBT operations
        NBTItem nbt = new NBTItem(itemStack);
        if (checkCoolDown(customItem, nbt, player)) {
            return;
        }

        nbt.setLong(Main.getNbtKey() + ".time", System.currentTimeMillis());
        itemStack = itemManager.computeUseNum(customItem, nbt);
        player.getInventory().setItemInMainHand(itemStack);

        // Play sound
        if (customItem.getSound() != null) {
            player.playSound(player.getLocation(), customItem.getSound(), (float) customItem.getVolume(), (float) customItem.getPitch());
        }

        // Send messages
        if (!customItem.getMessage().isEmpty()) {
            customItem.getMessage().forEach(msg -> itemManager.sendMessage(player, msg));
        }

        // Execute commands
        if (!customItem.getCommands().isEmpty()) {
            customItem.getCommands().forEach(command -> itemManager.executeCommands(player, command));
        }
    }

    private boolean canUseItem(CustomItem customItem, Action action, Player player) {
        boolean canUse;
        // Check item usage actions
        if (!customItem.getActions().isEmpty()) {
            canUse = customItem.getActions().stream().anyMatch(itemAction -> itemAction.equals(action));
            if (!canUse) {
                return false;
            }
        }
        // PlaceholderAPI support
        if (Main.isPapiEnable() && !customItem.getPlaceholder().isEmpty()) {
            canUse = customItem.getPlaceholder().stream().allMatch(papi -> itemManager.contentPlaceholder(player, papi));
            if (!canUse) {
                customItem.getPapi_message().forEach(msg -> itemManager.sendMessage(player, msg));
                return false;
            }
        }
        // Check permissions
        if (!customItem.getPermission().isEmpty()) {
            canUse = customItem.getPermission().stream().allMatch(player::hasPermission);
            if (!canUse) {
                customItem.getPer_message().forEach(msg -> itemManager.sendMessage(player, msg));
                return false;
            }
        }
        return true;
    }

    private boolean checkCoolDown(CustomItem customItem, NBTItem nbt, Player player) {
        if (nbt.hasTag(Main.getNbtKey())) {
            long time = nbt.getLong(Main.getNbtKey() + ".time");
            long cool = System.currentTimeMillis() - time;
            if (cool < customItem.getCool_down()) {
                customItem.getCool_message().forEach(msg -> itemManager.sendMessage(player, msg.replace("{cool}", String.valueOf((customItem.getCool_down() - cool) / 1000))));
                return true;
            }
        } else {
            nbt.setInteger(Main.getNbtKey() + ".use", 0);
        }
        return false;
    }


}
