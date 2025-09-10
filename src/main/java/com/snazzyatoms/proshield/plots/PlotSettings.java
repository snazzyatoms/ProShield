package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Stores per-claim settings and flags.
 * - Preserves all older settings (PvP, keep-items, etc.)
 * - Extended with new flags (fire, explosions, containers, redstone, animals)
 * - Supports defaults from config.yml
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean explosionsEnabled;
    private boolean fireEnabled;
    private boolean containersEnabled;
    private boolean redstoneEnabled;
    private boolean animalsEnabled;
    private boolean keepItemsEnabled;

    public PlotSettings() {
        // Defaults will be filled by PlotManager when loading from config
    }

    // === Getters / Setters ===

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isExplosionsEnabled() {
        return explosionsEnabled;
    }

    public void setExplosionsEnabled(boolean explosionsEnabled) {
        this.explosionsEnabled = explosionsEnabled;
    }

    public boolean isFireEnabled() {
        return fireEnabled;
    }

    public void setFireEnabled(boolean fireEnabled) {
        this.fireEnabled = fireEnabled;
    }

    public boolean isContainersEnabled() {
        return containersEnabled;
    }

    public void setContainersEnabled(boolean containersEnabled) {
        this.containersEnabled = containersEnabled;
    }

    public boolean isRedstoneEnabled() {
        return redstoneEnabled;
    }

    public void setRedstoneEnabled(boolean redstoneEnabled) {
        this.redstoneEnabled = redstoneEnabled;
    }

    public boolean isAnimalsEnabled() {
        return animalsEnabled;
    }

    public void setAnimalsEnabled(boolean animalsEnabled) {
        this.animalsEnabled = animalsEnabled;
    }

    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

    // === Persistence ===

    public void loadFromConfig(ConfigurationSection section, ConfigurationSection globalDefaults) {
        if (section == null) {
            // Use global defaults if per-claim section not present
            this.pvpEnabled = globalDefaults.getBoolean("pvp", false);
            this.explosionsEnabled = globalDefaults.getBoolean("explosions", false);
            this.fireEnabled = globalDefaults.getBoolean("fire", false);
            this.containersEnabled = globalDefaults.getBoolean("containers", true);
            this.redstoneEnabled = globalDefaults.getBoolean("redstone", true);
            this.animalsEnabled = globalDefaults.getBoolean("animals", true);
            this.keepItemsEnabled = globalDefaults.getBoolean("keep-items", false);
            return;
        }

        // Read per-claim overrides, fallback to global defaults
        this.pvpEnabled = section.getBoolean("pvp", globalDefaults.getBoolean("pvp", false));
        this.explosionsEnabled = section.getBoolean("explosions", globalDefaults.getBoolean("explosions", false));
        this.fireEnabled = section.getBoolean("fire", globalDefaults.getBoolean("fire", false));
        this.containersEnabled = section.getBoolean("containers", globalDefaults.getBoolean("containers", true));
        this.redstoneEnabled = section.getBoolean("redstone", globalDefaults.getBoolean("redstone", true));
        this.animalsEnabled = section.getBoolean("animals", globalDefaults.getBoolean("animals", true));
        this.keepItemsEnabled = section.getBoolean("keep-items", globalDefaults.getBoolean("keep-items", false));
    }

    public void saveToConfig(ConfigurationSection section) {
        if (section == null) return;

        section.set("pvp", this.pvpEnabled);
        section.set("explosions", this.explosionsEnabled);
        section.set("fire", this.fireEnabled);
        section.set("containers", this.containersEnabled);
        section.set("redstone", this.redstoneEnabled);
        section.set("animals", this.animalsEnabled);
        section.set("keep-items", this.keepItemsEnabled);
    }
}
