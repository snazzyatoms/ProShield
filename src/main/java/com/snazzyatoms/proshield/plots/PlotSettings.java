// path: src/main/java/com/snazzyatoms/proshield/plots/PlotSettings.java
package com.snazzyatoms.proshield.plots;

import java.io.Serializable;

/**
 * Per-claim settings for protection flags.
 * - Preserves all previous settings
 * - Extended with keepItemsEnabled flag and more toggles
 */
public class PlotSettings implements Serializable {

    private boolean pvpEnabled;
    private boolean explosionsEnabled;
    private boolean fireEnabled;
    private boolean interactionsEnabled;
    private boolean entityGriefEnabled;
    private boolean keepItemsEnabled; // NEW per-claim toggle for keep-items

    // Future extended flags (preserved from roadmap)
    private boolean redstoneEnabled;
    private boolean containerAccessEnabled;
    private boolean animalAccessEnabled;

    public PlotSettings() {
        this.pvpEnabled = false;
        this.explosionsEnabled = true;
        this.fireEnabled = true;
        this.interactionsEnabled = true;
        this.entityGriefEnabled = true;
        this.keepItemsEnabled = false;
        this.redstoneEnabled = true;
        this.containerAccessEnabled = true;
        this.animalAccessEnabled = true;
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

    // === Interactions ===
    public boolean isInteractionsEnabled() {
        return interactionsEnabled;
    }

    public void setInteractionsEnabled(boolean interactionsEnabled) {
        this.interactionsEnabled = interactionsEnabled;
    }

    // === Entity grief ===
    public boolean isEntityGriefEnabled() {
        return entityGriefEnabled;
    }

    public void setEntityGriefEnabled(boolean entityGriefEnabled) {
        this.entityGriefEnabled = entityGriefEnabled;
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

    // === Animal Access ===
    public boolean isAnimalAccessEnabled() {
        return animalAccessEnabled;
    }

    public void setAnimalAccessEnabled(boolean animalAccessEnabled) {
        this.animalAccessEnabled = animalAccessEnabled;
    }
}
