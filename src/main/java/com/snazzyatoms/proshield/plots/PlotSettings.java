package com.snazzyatoms.proshield.plots;

/**
 * Stores all per-claim settings for a plot.
 * - Extended to include keep-items, PvP, explosions, fire, and mob grief toggles
 * - Defaults can be overridden by global config or updated by players/admins
 */
public class PlotSettings {

    // === Existing settings (preserve everything you had before) ===
    private boolean allowBuild = true;
    private boolean allowInteract = true;
    private boolean allowContainers = true;

    // === New extended settings (1.2.5) ===
    private boolean keepItemsEnabled = false;   // per-claim override for item persistence
    private boolean pvpEnabled = false;         // per-claim PvP toggle
    private boolean explosionsEnabled = false;  // per-claim explosions toggle
    private boolean fireEnabled = false;        // per-claim fire spread/ignite toggle
    private boolean mobGriefEnabled = false;    // per-claim mob griefing toggle

    // --- Constructors ---
    public PlotSettings() {
        // Defaults already set above
    }

    // === Getters & Setters ===
    public boolean isAllowBuild() {
        return allowBuild;
    }

    public void setAllowBuild(boolean allowBuild) {
        this.allowBuild = allowBuild;
    }

    public boolean isAllowInteract() {
        return allowInteract;
    }

    public void setAllowInteract(boolean allowInteract) {
        this.allowInteract = allowInteract;
    }

    public boolean isAllowContainers() {
        return allowContainers;
    }

    public void setAllowContainers(boolean allowContainers) {
        this.allowContainers = allowContainers;
    }

    // --- Extended ---
    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

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

    public boolean isMobGriefEnabled() {
        return mobGriefEnabled;
    }

    public void setMobGriefEnabled(boolean mobGriefEnabled) {
        this.mobGriefEnabled = mobGriefEnabled;
    }

    @Override
    public String toString() {
        return "PlotSettings{" +
                "allowBuild=" + allowBuild +
                ", allowInteract=" + allowInteract +
                ", allowContainers=" + allowContainers +
                ", keepItemsEnabled=" + keepItemsEnabled +
                ", pvpEnabled=" + pvpEnabled +
                ", explosionsEnabled=" + explosionsEnabled +
                ", fireEnabled=" + fireEnabled +
                ", mobGriefEnabled=" + mobGriefEnabled +
                '}';
    }
}
