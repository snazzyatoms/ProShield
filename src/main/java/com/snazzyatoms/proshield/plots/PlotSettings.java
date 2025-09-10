package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores per-claim settings, with defaults from global config.
 * Players (owners/admins) can override these settings individually.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean keepDropsEnabled;
    private final Map<String, Boolean> itemRules = new HashMap<>();

    public PlotSettings() {
        // empty by default (values will be set from global config in initDefaults)
    }

    /**
     * Initialize claim settings from global config defaults.
     */
    public void initDefaults(ProShield plugin) {
        FileConfiguration config = plugin.getConfig();

        // === PvP ===
        this.pvpEnabled = config.getBoolean("protection.pvp-in-claims", false);

        // === Keep drops ===
        this.keepDropsEnabled = config.getBoolean("claims.keep-items.enabled", false);

        // === Item rules (defaults off unless defined) ===
        if (config.isConfigurationSection("claims.item-rules")) {
            for (String key : config.getConfigurationSection("claims.item-rules").getKeys(false)) {
                boolean def = config.getBoolean("claims.item-rules." + key, false);
                this.itemRules.put(key, def);
            }
        }
    }

    // === PvP ===
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    // === Keep Drops ===
    public boolean isKeepDropsEnabled() {
        return keepDropsEnabled;
    }

    public void setKeepDropsEnabled(boolean keepDropsEnabled) {
        this.keepDropsEnabled = keepDropsEnabled;
    }

    // === Item Rules ===
    public Map<String, Boolean> getItemRules() {
        return itemRules;
    }

    public boolean getItemRule(String key) {
        return itemRules.getOrDefault(key, false);
    }

    public void setItemRule(String key, boolean enabled) {
        itemRules.put(key, enabled);
    }

    // === Debug dump (optional helper) ===
    @Override
    public String toString() {
        return "PlotSettings{" +
                "pvpEnabled=" + pvpEnabled +
                ", keepDropsEnabled=" + keepDropsEnabled +
                ", itemRules=" + itemRules +
                '}';
    }
}
