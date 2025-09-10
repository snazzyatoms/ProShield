package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Stores per-claim settings such as PvP, item-keep, and flags.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean keepItemsEnabled;

    public PlotSettings() {
        this.pvpEnabled = false;       // default: PvP disabled
        this.keepItemsEnabled = false; // default: no keep-items
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

    /**
     * Load settings from config section.
     */
    public void load(ConfigurationSection section) {
        if (section == null) return;
        this.pvpEnabled = section.getBoolean("pvp", false);
        this.keepItemsEnabled = section.getBoolean("keep-items", false);
    }

    /**
     * Save settings into config section.
     */
    public void save(ConfigurationSection section) {
        if (section == null) return;
        section.set("pvp", this.pvpEnabled);
        section.set("keep-items", this.keepItemsEnabled);
    }
}
