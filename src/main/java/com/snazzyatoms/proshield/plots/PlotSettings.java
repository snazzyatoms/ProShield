package com.snazzyatoms.proshield.plots;

/**
 * Stores per-claim settings such as PvP, explosions, item-keep, and item-protection.
 * These settings override global config values if toggled by the claim owner.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean explosionsEnabled;
    private boolean fireSpreadEnabled;

    // === NEW: per-claim item keep toggle ===
    private boolean keepItemsEnabled;

    // === NEW: per-claim item protection toggle ===
    private boolean itemProtectionEnabled;

    public PlotSettings() {
        this.pvpEnabled = false;               // default -> follow global config
        this.explosionsEnabled = true;         // default -> allow unless overridden
        this.fireSpreadEnabled = true;         // default -> allow unless overridden
        this.keepItemsEnabled = false;         // default -> inherit from global config
        this.itemProtectionEnabled = true;     // default -> inherit from global config
    }

    // PvP
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    // Explosions
    public boolean isExplosionsEnabled() {
        return explosionsEnabled;
    }

    public void setExplosionsEnabled(boolean explosionsEnabled) {
        this.explosionsEnabled = explosionsEnabled;
    }

    // Fire Spread
    public boolean isFireSpreadEnabled() {
        return fireSpreadEnabled;
    }

    public void setFireSpreadEnabled(boolean fireSpreadEnabled) {
        this.fireSpreadEnabled = fireSpreadEnabled;
    }

    // === NEW Keep Items ===
    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

    // === NEW Item Protection ===
    public boolean isItemProtectionEnabled() {
        return itemProtectionEnabled;
    }

    public void setItemProtectionEnabled(boolean itemProtectionEnabled) {
        this.itemProtectionEnabled = itemProtectionEnabled;
    }
}
