package com.snazzyatoms.proshield.plots;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores per-claim settings, merged with global defaults but
 * configurable per-claim. Persisted via PlotManager.
 */
public class PlotSettings {

    // === Core Settings ===
    private boolean pvpEnabled;
    private boolean keepDropsEnabled;

    // Item rules (extra granularity per-claim)
    private final Set<String> allowedItems = new HashSet<>();
    private final Set<String> blockedItems = new HashSet<>();

    public PlotSettings() {
        // defaults: read from global config later in loadFromConfig
        this.pvpEnabled = false;
        this.keepDropsEnabled = false;
    }

    // ==================================================
    // Getters & Setters
    // ==================================================

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isKeepDropsEnabled() {
        return keepDropsEnabled;
    }

    public void setKeepDropsEnabled(boolean keepDropsEnabled) {
        this.keepDropsEnabled = keepDropsEnabled;
    }

    public Set<String> getAllowedItems() {
        return allowedItems;
    }

    public Set<String> getBlockedItems() {
        return blockedItems;
    }

    // ==================================================
    // Persistence
    // ==================================================

    public void saveToConfig(FileConfiguration config, String path) {
        config.set(path + ".pvp-enabled", pvpEnabled);
        config.set(path + ".keep-drops-enabled", keepDropsEnabled);
        config.set(path + ".allowed-items", new HashSet<>(allowedItems));
        config.set(path + ".blocked-items", new HashSet<>(blockedItems));
    }

    @SuppressWarnings("unchecked")
    public void loadFromConfig(FileConfiguration config, String path) {
        this.pvpEnabled = config.getBoolean(path + ".pvp-enabled",
                config.getBoolean("protection.pvp-in-claims", false));
        this.keepDropsEnabled = config.getBoolean(path + ".keep-drops-enabled",
                config.getBoolean("claims.keep-items.enabled", false));

        this.allowedItems.clear();
        this.blockedItems.clear();

        this.allowedItems.addAll(config.getStringList(path + ".allowed-items"));
        this.blockedItems.addAll(config.getStringList(path + ".blocked-items"));
    }
}
