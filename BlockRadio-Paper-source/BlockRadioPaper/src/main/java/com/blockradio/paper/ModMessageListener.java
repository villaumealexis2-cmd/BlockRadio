package com.blockradio.paper;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

/** Reçoit les messages du mod : détection (HELLO) et état de la radio (STATE). */
public final class ModMessageListener implements PluginMessageListener, Listener {

    private static final int MAX_MESSAGES_PER_SECOND = 30;
    private static final int MAX_TITLE_CHARS = 100;
    private static final int MAX_VERSION_CHARS = 32;

    private final BlockRadioPlugin plugin;

    public ModMessageListener(BlockRadioPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!Protocol.CHANNEL.equals(channel)) {
            return;
        }
        try {
            handle(player, new Protocol.Reader(message));
        } catch (Protocol.MalformedMessageException e) {
            plugin.getLogger().fine("Message invalide de " + player.getName() + " : " + e.getMessage());
        }
    }

    private void handle(Player player, Protocol.Reader in) {
        int type = in.readByte();
        switch (type) {
            case Protocol.C2S_HELLO -> handleHello(player, in);
            case Protocol.C2S_STATE -> handleState(player, in);
            default -> { }
        }
    }

    private void handleHello(Player player, Protocol.Reader in) {
        int version = in.readVarInt();
        String modVersion = Protocol.sanitize(in.readString(), MAX_VERSION_CHARS);
        if (version != Protocol.VERSION) {
            plugin.getLogger().warning(player.getName() + " utilise le protocole BlockRadio " + version
                    + " (attendu : " + Protocol.VERSION + "), ignoré.");
            return;
        }
        if (plugin.sessions().putIfAbsent(player.getUniqueId(), new RadioSession(modVersion)) == null) {
            plugin.getLogger().info(player.getName() + " est équipé de BlockRadio " + modVersion);
        }
    }

    private void handleState(Player player, Protocol.Reader in) {
        RadioSession session = plugin.sessions().get(player.getUniqueId());
        if (session == null || !allow(session)) {
            return;
        }

        boolean playing = in.readByte() != 0;
        int volume = Math.max(0, Math.min(100, in.readVarInt()));
        String title = Protocol.sanitize(in.readString(), MAX_TITLE_CHARS);

        session.playing = playing;
        session.volume = volume;
        session.title = title;

        plugin.bossBars().update(player, session);
        plugin.refreshMenu(player, session);
    }

    /** Limite le nombre de messages par seconde pour un client modifié qui spammerait. */
    private static boolean allow(RadioSession session) {
        long now = System.currentTimeMillis();
        if (now - session.windowStart >= 1000) {
            session.windowStart = now;
            session.windowCount = 0;
        }
        return ++session.windowCount <= MAX_MESSAGES_PER_SECOND;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        RadioSession session = plugin.sessions().remove(id);
        if (session != null) {
            plugin.bossBars().hide(event.getPlayer(), session);
        }
    }
}
