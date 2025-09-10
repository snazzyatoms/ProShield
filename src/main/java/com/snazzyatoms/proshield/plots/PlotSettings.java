package com.snazzyatoms.proshield.plots;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents per-claim settings & flags.
 * Extends global defaults with player overrides.
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean keepItemsEnabled;

    private boolean damageEnabled;
    private boolean pveEnabled;
    private boolean entityGriefingAllowed;
    private boolean itemFramesAllowed;
    private boolean vehiclesAllowed;
    private boolean bucketsAllowed;
    private boolean redstoneAllowed;
    private boolean containersAllowed;
    private boolean animalAccessAllowed;

    private final Set<String> customFlags = new HashSet<>();

    public PlotSettings() {
        // Defaults (can be overridden per-claim or from config)
        this.pvpEnabled = false;
        this.keepItemsEnabled = false;
        this.damageEnabled = true;
        this.pveEnabled = true;
        this.entityGriefingAllowed = false;
        this.itemFramesAllowed = false;
        this.vehiclesAllowed = false;
        this.bucketsAllowed = false;
        this.redstoneAllowed = true;
        this.containersAllowed = true;
        this.animalAccessAllowed = false;
    }

    // =====================
    // ✅ Core Toggles
    // =====================

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

    // =====================
    // ✅ Protection Flags
    // =====================

    public boolean isDamageEnabled() {
        return damageEnabled;
    }

    public void setDamageEnabled(boolean damageEnabled) {
        this.damageEnabled = damageEnabled;
    }

    public boolean isPveEnabled() {
        return pveEnabled;
    }

    public void setPveEnabled(boolean pveEnabled) {
        this.pveEnabled = pveEnabled;
    }

    public boolean isEntityGriefingAllowed() {
        return entityGriefingAllowed;
    }

    public void setEntityGriefingAllowed(boolean entityGriefingAllowed) {
        this.entityGriefingAllowed = entityGriefingAllowed;
    }

    public boolean isItemFramesAllowed() {
        return itemFramesAllowed;
    }

    public void setItemFramesAllowed(boolean itemFramesAllowed) {
        this.itemFramesAllowed = itemFramesAllowed;
    }

    public boolean isVehiclesAllowed() {
        return vehiclesAllowed;
    }

    public void setVehiclesAllowed(boolean vehiclesAllowed) {
        this.vehiclesAllowed = vehiclesAllowed;
    }

    public boolean isBucketsAllowed() {
        return bucketsAllowed;
    }

    public void setBucketsAllowed(boolean bucketsAllowed) {
        this.bucketsAllowed = bucketsAllowed;
    }

    public boolean isRedstoneAllowed() {
        return redstoneAllowed;
    }

    public void setRedstoneAllowed(boolean redstoneAllowed) {
        this.redstoneAllowed = redstoneAllowed;
    }

    public boolean isContainersAllowed() {
        return containersAllowed;
    }

    public void setContainersAllowed(boolean containersAllowed) {
        this.containersAllowed = containersAllowed;
    }

    public boolean isAnimalAccessAllowed() {
        return animalAccessAllowed;
    }

    public void setAnimalAccessAllowed(boolean animalAccessAllowed) {
        this.animalAccessAllowed = animalAccessAllowed;
    }

    // =====================
    // ✅ Custom Flags
    // =====================

    public void addFlag(String flag) {
        customFlags.add(flag.toLowerCase());
    }

    public void removeFlag(String flag) {
        customFlags.remove(flag.toLowerCase());
    }

    public boolean hasFlag(String flag) {
        return customFlags.contains(flag.toLowerCase());
    }

    public Set<String> getFlags() {
        return customFlags;
    }
}
