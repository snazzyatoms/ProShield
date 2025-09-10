package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Per-claim settings for ProShield.
 * Stored alongside claim ownership & trusted players.
 * Supports merging with global config defaults.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean keepDropsEnabled;

    // Future-proof: we can add more toggles like explosions/fire/etc.

    public PlotSettings() {
        // defaults
        this.pvpEnabled = false;          // global default (from config)
        this.keepDropsEnabled = false;    // global default (from config)
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean enabled) {
        this.pvpEnabled = enabled;
    }

    public boolean isKeepDropsEnabled() {
        return keepDropsEnabled;
    }

    public void setKeepDropsEnabled(boolean enabled) {
        this.keepDropsEnabled = enabled;
    }

    /**
     * Load per-claim settings from a YAML section.
     * Falls back to defaults if missing.
     */
    public static PlotSettings fromConfig(ConfigurationSection section) {
        PlotSettings settings = new PlotSettings();

        if (section == null) return settings;

        settings.setPvpEnabled(section.getBoolean("pvp", false));
        settings.setKeepDropsEnabled(section.getBoolean("keep-drops", false));

        return settings;
    }

    /**
     * Save per-claim settings to YAML.
     */
    public void saveToConfig(ConfigurationSection section) {
        section.set("pvp", this.pvpEnabled);
        section.set("keep-drops", this.keepDropsEnabled);
    }

    @Override
    public String toString() {
        return "PlotSettings{" +
                "pvpEnabled=" + pvpEnabled +
                ", keepDropsEnabled=" + keepDropsEnabled +
                '}';
    }
}
