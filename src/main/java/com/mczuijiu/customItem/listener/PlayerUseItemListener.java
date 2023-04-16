package com.mczuijiu.customItem.listener;

import com.mczuijiu.customItem.Main;
import com.mczuijiu.customItem.item.CustomItem;
import com.mczuijiu.customItem.manager.ItemManager;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
        AtomicBoolean canUse = new AtomicBoolean(true);

        if (customItem == null) {
            return;
        }
        event.setCancelled(true);

        // 物品使用动作
        if (!customItem.getActions().isEmpty()) {
            customItem.getActions().forEach(action -> {
                if (!action.equals(event.getAction())) {
                    canUse.set(false);
                }
            });
            if (!canUse.get()) {
                return;
            }
        }

        // PAPI变量支持
        if (Main.isPapiEnable() && !customItem.getPlaceholder().isEmpty()) {
            customItem.getPlaceholder().forEach(papi -> {
                if (!itemManager.contentPlaceholder(player, papi)) {
                    canUse.set(false);
                }
            });
            if (!canUse.get()) {
                customItem.getPapi_message().forEach(msg -> itemManager.sendMessage(player, msg));
                return;
            }
        }

        // 权限
        if (!customItem.getPermission().isEmpty()) {
            customItem.getPermission().forEach(permission -> {
                if (!player.hasPermission(permission)) {
                    canUse.set(false);
                }
            });
            if (!canUse.get()) {
                customItem.getPer_message().forEach(msg -> itemManager.sendMessage(player, msg));
                return;
            }
        }

        // nbt操作
        NBTItem nbt = new NBTItem(itemStack);
        if (nbt.hasTag(Main.getNbtKey())) {
            long time = nbt.getLong(Main.getNbtKey() + ".time");
            long cool = System.currentTimeMillis() - time;
            if (cool < customItem.getCool_down()) {
                customItem.getCool_message().forEach(msg -> itemManager.sendMessage(player,
                        msg.replace("{cool}", String.valueOf((customItem.getCool_down() - cool)/1000))));
                return;
            }
        } else {
            nbt.setInteger(Main.getNbtKey() + ".use", 0);
        }

        nbt.setLong(Main.getNbtKey() + ".time", System.currentTimeMillis());
        itemStack = itemManager.computeUseNum(customItem, nbt);
        player.getInventory().setItemInMainHand(itemStack);

        // 播放音效
        if (customItem.getSound() != null) {
            player.playSound(player.getLocation(), customItem.getSound(), (float) customItem.getVolume(), (float) customItem.getPitch());
        }

        // 发送消息
        if (!customItem.getMessage().isEmpty()) {
            customItem.getMessage().forEach(msg -> itemManager.sendMessage(player, msg));
        }

        // 执行命令
        if (!customItem.getCommands().isEmpty()) {
            customItem.getCommands().forEach(command -> itemManager.executeCommands(player, command));
        }

    }

}
