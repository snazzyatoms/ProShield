package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores per-claim protection flags.
 * - Defaults come from config.yml
 * - Can be toggled per-claim via GUI or commands
 */
public class PlotSettings {

    private boolean pvpEnabled;
    private boolean explosionsAllowed;
    private boolean fireSpreadAllowed;
    private boolean entityGriefingAllowed;
    private boolean keepItemsEnabled;
    private boolean damageEnabled;
    private boolean pveEnabled;
    private boolean bucketsAllowed;
    private boolean itemFramesAllowed;
    private boolean vehiclesAllowed;
    private boolean redstoneAllowed;
    private boolean containerAccessAllowed;
    private boolean animalAccessAllowed;

    /** Flexible storage for future flags */
    private final Map<String, Boolean> customFlags = new HashMap<>();

    public PlotSettings() {
        // Default values (synced with config.yml global defaults)
        this.pvpEnabled = false;
        this.explosionsAllowed = false;
        this.fireSpreadAllowed = false;
        this.entityGriefingAllowed = false;
        this.keepItemsEnabled = false;
        this.damageEnabled = true;
        this.pveEnabled = true;
        this.bucketsAllowed = false;
        this.itemFramesAllowed = false;
        this.vehiclesAllowed = false;
        this.redstoneAllowed = true;
        this.containerAccessAllowed = false;
        this.animalAccessAllowed = false;
    }

    /* ---------------------------------------------------------
     * Standard getters / setters
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

    public boolean isFireSpreadAllowed() {
        return fireSpreadAllowed;
    }

    public void setFireSpreadAllowed(boolean fireSpreadAllowed) {
        this.fireSpreadAllowed = fireSpreadAllowed;
    }

    public boolean isEntityGriefingAllowed() {
        return entityGriefingAllowed;
    }

    public void setEntityGriefingAllowed(boolean entityGriefingAllowed) {
        this.entityGriefingAllowed = entityGriefingAllowed;
    }

    public boolean isKeepItemsEnabled() {
        return keepItemsEnabled;
    }

    public void setKeepItemsEnabled(boolean keepItemsEnabled) {
        this.keepItemsEnabled = keepItemsEnabled;
    }

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

    public boolean isBucketsAllowed() {
        return bucketsAllowed;
    }

    public void setBucketsAllowed(boolean bucketsAllowed) {
        this.bucketsAllowed = bucketsAllowed;
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

    public boolean isAnimalAccessAllowed() {
        return animalAccessAllowed;
    }

    public void setAnimalAccessAllowed(boolean animalAccessAllowed) {
        this.animalAccessAllowed = animalAccessAllowed;
    }

    /* ---------------------------------------------------------
     * Custom flags (future proofing)
     * --------------------------------------------------------- */

    public boolean isFlagEnabled(String key) {
        return customFlags.getOrDefault(key.toLowerCase(), false);
    }

    public void setFlag(String key, boolean value) {
        customFlags.put(key.toLowerCase(), value);
    }

    public Map<String, Boolean> getAllFlags() {
        Map<String, Boolean> all = new HashMap<>(customFlags);
        all.put("pvp", pvpEnabled);
        all.put("explosions", explosionsAllowed);
        all.put("fire", fireSpreadAllowed);
        all.put("entitygrief", entityGriefingAllowed);
        all.put("keepitems", keepItemsEnabled);
        all.put("damage", damageEnabled);
        all.put("pve", pveEnabled);
        all.put("buckets", bucketsAllowed);
        all.put("itemframes", itemFramesAllowed);
        all.put("vehicles", vehiclesAllowed);
        all.put("redstone", redstoneAllowed);
        all.put("containers", containerAccessAllowed);
        all.put("animals", animalAccessAllowed);
        return all;
    }
}
