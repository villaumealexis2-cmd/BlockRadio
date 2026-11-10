package com.blockradio.paper;

import java.util.Locale;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/** Gère la boss bar du titre en cours, une par joueur équipé. */
public final class BossBarService {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final BlockRadioPlugin plugin;

    public BossBarService(BlockRadioPlugin plugin) {
        this.plugin = plugin;
    }

    public void update(Player player, RadioSession session) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("bossbar.enabled", true) || !session.playing) {
            hide(player, session);
            return;
        }

        String shown = session.title.isEmpty()
                ? config.getString("bossbar.unknown-title", "Radio")
                : session.title;
        Component name = MM.deserialize(
                config.getString("bossbar.format", "<gold>♪ <white><title>"),
                Placeholder.unparsed("title", shown));

        if (session.bossBar == null) {
            session.bossBar = BossBar.bossBar(name, 1.0f, color(config), overlay(config));
            player.showBossBar(session.bossBar);
        } else {
            session.bossBar.name(name);
        }
    }

    public void hide(Player player, RadioSession session) {
        if (session.bossBar != null) {
            player.hideBossBar(session.bossBar);
            session.bossBar = null;
        }
    }

    private static BossBar.Color color(FileConfiguration config) {
        try {
            return BossBar.Color.valueOf(config.getString("bossbar.color", "PURPLE").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Color.PURPLE;
        }
    }

    private static BossBar.Overlay overlay(FileConfiguration config) {
        try {
            return BossBar.Overlay.valueOf(config.getString("bossbar.overlay", "PROGRESS").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Overlay.PROGRESS;
        }
    }
}
