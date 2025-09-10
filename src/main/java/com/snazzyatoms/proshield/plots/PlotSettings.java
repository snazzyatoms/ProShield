package com.snazzyatoms.proshield.plots;

/**
 * Per-claim settings for protection and customization.
 * 
 * These settings override global config values when enabled.
 * They allow claim owners to decide what happens inside their claim
 * (e.g., PvP, explosions, fire, mob grief, item keep).
 */
public class PlotSettings {

    // === Claim-specific toggles ===
    private boolean keepItemsEnabled = false;
    private boolean pvpEnabled = false;
    private boolean explosionsEnabled = false;
    private boolean fireEnabled = false;
    private boolean mobGriefEnabled = false;

    // Future-proof: could add more flags here (e.g. redstone, animals, etc.)

    public PlotSettings() {
    }

    // === Keep Items ===
    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
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

    // === Fire Spread ===
    public boolean isFireEnabled() {
        return fireEnabled;
    }

    public void setFireEnabled(boolean fireEnabled) {
        this.fireEnabled = fireEnabled;
    }

    // === Mob Grief (e.g. Endermen, Ravagers, Withers) ===
    public boolean isMobGriefEnabled() {
        return mobGriefEnabled;
    }

    public void setMobGriefEnabled(boolean mobGriefEnabled) {
        this.mobGriefEnabled = mobGriefEnabled;
    }

    @Override
    public String toString() {
        return "PlotSettings{" +
                "keepItemsEnabled=" + keepItemsEnabled +
                ", pvpEnabled=" + pvpEnabled +
                ", explosionsEnabled=" + explosionsEnabled +
                ", fireEnabled=" + fireEnabled +
                ", mobGriefEnabled=" + mobGriefEnabled +
                '}';
    }
}
