package com.blockradio.paper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

public final class BlockRadioPlugin extends JavaPlugin {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final long COMMAND_COOLDOWN_MILLIS = 150;

    private final Map<UUID, RadioSession> sessions = new ConcurrentHashMap<>();
    private BossBarService bossBars;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bossBars = new BossBarService(this);

        ModMessageListener listener = new ModMessageListener(this);
        Messenger messenger = getServer().getMessenger();
        messenger.registerIncomingPluginChannel(this, Protocol.CHANNEL, listener);
        messenger.registerOutgoingPluginChannel(this, Protocol.CHANNEL);

        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        PluginCommand command = getCommand("musique");
        if (command != null) {
            command.setExecutor(new MusicCommand(this));
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            RadioSession session = sessions.get(player.getUniqueId());
            if (session != null) {
                bossBars.hide(player, session);
            }
        }
        sessions.clear();

        Messenger messenger = getServer().getMessenger();
        messenger.unregisterIncomingPluginChannel(this);
        messenger.unregisterOutgoingPluginChannel(this);
    }

    public Map<UUID, RadioSession> sessions() {
        return sessions;
    }

    public BossBarService bossBars() {
        return bossBars;
    }

    /** Texte MiniMessage lu dans config.yml. */
    public Component msg(String path, TagResolver... resolvers) {
        return MM.deserialize(getConfig().getString(path, path), resolvers);
    }

    /** Envoie un ordre au mod du joueur ; sans effet s'il n'est pas équipé. */
    public void sendAction(Player player, Protocol.Action action) {
        RadioSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - session.lastCommandMillis < COMMAND_COOLDOWN_MILLIS) {
            return;
        }
        session.lastCommandMillis = now;
        player.sendPluginMessage(this, Protocol.CHANNEL, Protocol.command(action));
    }

    /** Met à jour le menu s'il est ouvert chez ce joueur. */
    public void refreshMenu(Player player, RadioSession session) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof MusicMenu menu) {
            menu.render(session);
        }
    }
}
