package com.blockradio.paper;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/** Gère les clics dans le menu : rien ne peut être pris ou déplacé, seuls les boutons agissent. */
public final class MenuListener implements Listener {

    private final BlockRadioPlugin plugin;

    public MenuListener(BlockRadioPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof MusicMenu)) {
            return;
        }
        event.setCancelled(true);

        if (event.getClickedInventory() != top || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Protocol.Action action = switch (event.getSlot()) {
            case MusicMenu.SLOT_PLAY -> Protocol.Action.PLAY;
            case MusicMenu.SLOT_STOP -> Protocol.Action.STOP;
            case MusicMenu.SLOT_VOLUME_UP -> Protocol.Action.VOLUME_UP;
            case MusicMenu.SLOT_VOLUME_DOWN -> Protocol.Action.VOLUME_DOWN;
            default -> null;
        };
        if (action != null) {
            plugin.sendAction(player, action);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MusicMenu) {
            event.setCancelled(true);
        }
    }
}
