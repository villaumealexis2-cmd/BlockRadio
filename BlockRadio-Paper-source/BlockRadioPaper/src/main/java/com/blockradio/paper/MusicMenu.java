package com.blockradio.paper;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Menu vanilla (coffre de 9 cases). Le holder sert à reconnaître l'inventaire dans les évènements. */
public final class MusicMenu implements InventoryHolder {

    public static final int SLOT_VOLUME_DOWN = 1;
    public static final int SLOT_PLAY = 2;
    public static final int SLOT_INFO = 4;
    public static final int SLOT_STOP = 6;
    public static final int SLOT_VOLUME_UP = 7;

    private final BlockRadioPlugin plugin;
    private final Inventory inventory;

    public MusicMenu(BlockRadioPlugin plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 9, plugin.msg("menu.title"));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void render(RadioSession session) {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setHideTooltip(true);
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(SLOT_VOLUME_DOWN, item(Material.ORANGE_CONCRETE, plugin.msg("menu.volume-down"), List.of()));
        inventory.setItem(SLOT_PLAY, item(Material.LIME_CONCRETE, plugin.msg("menu.play"), List.of()));
        inventory.setItem(SLOT_STOP, item(Material.RED_CONCRETE, plugin.msg("menu.stop"), List.of()));
        inventory.setItem(SLOT_VOLUME_UP, item(Material.YELLOW_CONCRETE, plugin.msg("menu.volume-up"), List.of()));

        List<Component> lore = new ArrayList<>();
        lore.add(plugin.msg(session.playing ? "menu.info-playing" : "menu.info-stopped"));
        if (session.playing && !session.title.isEmpty()) {
            lore.add(plugin.msg("menu.info-title", Placeholder.unparsed("title", session.title)));
        }
        String volume = session.volume < 0 ? "?" : Integer.toString(session.volume);
        lore.add(plugin.msg("menu.info-volume", Placeholder.unparsed("volume", volume)));
        inventory.setItem(SLOT_INFO, item(Material.MUSIC_DISC_13, plugin.msg("menu.info-name"), lore));
    }

    private static ItemStack item(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        if (!lore.isEmpty()) {
            List<Component> fixed = new ArrayList<>();
            for (Component line : lore) {
                fixed.add(line.decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(fixed);
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
