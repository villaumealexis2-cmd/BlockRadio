package com.blockradio.paper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /musique : ouvre le menu, uniquement pour les joueurs équipés du mod. */
public final class MusicCommand implements CommandExecutor {

    private final BlockRadioPlugin plugin;

    public MusicCommand(BlockRadioPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfig().getString("messages.player-only", "Réservé aux joueurs."));
            return true;
        }
        RadioSession session = plugin.sessions().get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(plugin.msg("messages.mod-required"));
            return true;
        }
        MusicMenu menu = new MusicMenu(plugin);
        menu.render(session);
        player.openInventory(menu.getInventory());
        return true;
    }
}
