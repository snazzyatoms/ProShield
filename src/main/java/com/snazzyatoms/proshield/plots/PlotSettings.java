package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores per-claim settings such as PvP, explosions, fire, and item persistence.
 * Extended to support keep-items toggle.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean explosionsEnabled;
    private boolean fireEnabled;

    // New: per-claim keep-items toggle
    private Boolean keepItemsEnabled; // nullable → null = fallback to global

    // Generic future-proof map for custom flags
    private final Map<String, Object> customFlags = new HashMap<>();

    public PlotSettings() {
        this.pvpEnabled = false;
        this.explosionsEnabled = true;
        this.fireEnabled = true;
        this.keepItemsEnabled = null; // default → defer to global
    }

    // === PvP ===
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    // === Explosions ===
    public boolean isExplosionsEnabled() {
        return explosionsEnabled;
    }

    public void setExplosionsEnabled(boolean explosionsEnabled) {
        this.explosionsEnabled = explosionsEnabled;
    }

    // === Fire ===
    public boolean isFireEnabled() {
        return fireEnabled;
    }

    public void setFireEnabled(boolean fireEnabled) {
        this.fireEnabled = fireEnabled;
    }

    // === Keep Items ===
    public Boolean getKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(Boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

    // === Custom Flags ===
    public Map<String, Object> getCustomFlags() {
        return customFlags;
    }

    public void setCustomFlag(String key, Object value) {
        customFlags.put(key, value);
    }

    public Object getCustomFlag(String key) {
        return customFlags.get(key);
    }
}
