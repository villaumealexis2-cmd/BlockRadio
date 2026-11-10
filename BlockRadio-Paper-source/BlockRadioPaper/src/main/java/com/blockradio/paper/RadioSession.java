package com.blockradio.paper;

import net.kyori.adventure.bossbar.BossBar;

/** État connu d'un joueur équipé du mod. */
public final class RadioSession {

    public final String modVersion;

    public boolean playing;
    /** -1 tant que le mod n'a pas envoyé son état. */
    public int volume = -1;
    public String title = "";

    BossBar bossBar;
    long windowStart;
    int windowCount;
    long lastCommandMillis;

    public RadioSession(String modVersion) {
        this.modVersion = modVersion;
    }
}
