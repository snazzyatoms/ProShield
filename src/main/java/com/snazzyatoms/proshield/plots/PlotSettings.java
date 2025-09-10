package com.snazzyatoms.proshield.plots;

/**
 * Holds per-claim configurable settings (flags).
 * These are merged with global config defaults.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean explosionsEnabled;
    private boolean fireEnabled;
    private boolean mobGriefEnabled;
    private boolean keepItemsEnabled;
    private boolean redstoneEnabled;
    private boolean containerAccessEnabled;
    private boolean animalInteractEnabled;

    public PlotSettings() {
        // defaults from global config (safety-first)
        this.pvpEnabled = false;
        this.explosionsEnabled = false;
        this.fireEnabled = false;
        this.mobGriefEnabled = false;
        this.keepItemsEnabled = false;
        this.redstoneEnabled = true;
        this.containerAccessEnabled = true;
        this.animalInteractEnabled = true;
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

    // === Mob Grief ===
    public boolean isMobGriefEnabled() {
        return mobGriefEnabled;
    }

    public void setMobGriefEnabled(boolean mobGriefEnabled) {
        this.mobGriefEnabled = mobGriefEnabled;
    }

    // === Keep Items ===
    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

    // === Redstone ===
    public boolean isRedstoneEnabled() {
        return redstoneEnabled;
    }

    public void setRedstoneEnabled(boolean redstoneEnabled) {
        this.redstoneEnabled = redstoneEnabled;
    }

    // === Container Access ===
    public boolean isContainerAccessEnabled() {
        return containerAccessEnabled;
    }

    public void setContainerAccessEnabled(boolean containerAccessEnabled) {
        this.containerAccessEnabled = containerAccessEnabled;
    }

    // === Animal Interact ===
    public boolean isAnimalInteractEnabled() {
        return animalInteractEnabled;
    }

    public void setAnimalInteractEnabled(boolean animalInteractEnabled) {
        this.animalInteractEnabled = animalInteractEnabled;
    }

    @Override
    public String toString() {
        return "PlotSettings{" +
                "pvpEnabled=" + pvpEnabled +
                ", explosionsEnabled=" + explosionsEnabled +
                ", fireEnabled=" + fireEnabled +
                ", mobGriefEnabled=" + mobGriefEnabled +
                ", keepItemsEnabled=" + keepItemsEnabled +
                ", redstoneEnabled=" + redstoneEnabled +
                ", containerAccessEnabled=" + containerAccessEnabled +
                ", animalInteractEnabled=" + animalInteractEnabled +
                '}';
    }
}
