package com.snazzyatoms.proshield.plots;

/**
 * Stores per-claim protection settings (flags).
 * Synced with config.yml defaults and preserved from earlier versions.
 */
public class PlotSettings {

    // === Core toggles ===
    private boolean pvpEnabled;
    private boolean explosionsAllowed;
    private boolean fireAllowed;
    private boolean bucketsAllowed;

    // === Extended toggles ===
    private boolean itemKeepEnabled;
    private boolean entityGriefingAllowed;
    private boolean interactionsAllowed;
    private boolean redstoneAllowed;
    private boolean containerAccessAllowed;

    // === Entities / items ===
    private boolean itemFramesAllowed;
    private boolean vehiclesAllowed;

    public PlotSettings() {
        // Default values synced with config.yml
        this.pvpEnabled = false;
        this.explosionsAllowed = false;
        this.fireAllowed = false;
        this.bucketsAllowed = false;

        this.itemKeepEnabled = false;
        this.entityGriefingAllowed = false;
        this.interactionsAllowed = true;
        this.redstoneAllowed = true;
        this.containerAccessAllowed = true;

        this.itemFramesAllowed = true;
        this.vehiclesAllowed = true;
    }

    /* ---------------------------------------------------------
     * Getters / Setters
     * --------------------------------------------------------- */

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isExplosionsAllowed() {
        return explosionsAllowed;
    }

    public void setExplosionsAllowed(boolean explosionsAllowed) {
        this.explosionsAllowed = explosionsAllowed;
    }

    public boolean isFireAllowed() {
        return fireAllowed;
    }

    public void setFireAllowed(boolean fireAllowed) {
        this.fireAllowed = fireAllowed;
    }

    public boolean isBucketsAllowed() {
        return bucketsAllowed;
    }

    public void setBucketsAllowed(boolean bucketsAllowed) {
        this.bucketsAllowed = bucketsAllowed;
    }

    public boolean isKeepItemsEnabled() {
        return itemKeepEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.itemKeepEnabled = keepItemsEnabled;
    }

    public boolean isEntityGriefingAllowed() {
        return entityGriefingAllowed;
    }

    public void setEntityGriefingAllowed(boolean entityGriefingAllowed) {
        this.entityGriefingAllowed = entityGriefingAllowed;
    }

    public boolean isInteractionsAllowed() {
        return interactionsAllowed;
    }

    public void setInteractionsAllowed(boolean interactionsAllowed) {
        this.interactionsAllowed = interactionsAllowed;
    }

    public boolean isRedstoneAllowed() {
        return redstoneAllowed;
    }

    public void setRedstoneAllowed(boolean redstoneAllowed) {
        this.redstoneAllowed = redstoneAllowed;
    }

    public boolean isContainerAccessAllowed() {
        return containerAccessAllowed;
    }

    public void setContainerAccessAllowed(boolean containerAccessAllowed) {
        this.containerAccessAllowed = containerAccessAllowed;
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

    @Override
    public String toString() {
        return "PlotSettings{" +
                "pvpEnabled=" + pvpEnabled +
                ", explosionsAllowed=" + explosionsAllowed +
                ", fireAllowed=" + fireAllowed +
                ", bucketsAllowed=" + bucketsAllowed +
                ", itemKeepEnabled=" + itemKeepEnabled +
                ", entityGriefingAllowed=" + entityGriefingAllowed +
                ", interactionsAllowed=" + interactionsAllowed +
                ", redstoneAllowed=" + redstoneAllowed +
                ", containerAccessAllowed=" + containerAccessAllowed +
                ", itemFramesAllowed=" + itemFramesAllowed +
                ", vehiclesAllowed=" + vehiclesAllowed +
                '}';
    }
}
