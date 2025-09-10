package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores per-claim settings and flags.
 * Preserves all existing logic, extended with new flags for v1.2.5.
 */
public class PlotSettings {

    private boolean pvpEnabled = false;
    private boolean keepItemsEnabled = false;

    private boolean explosionsAllowed = true;
    private boolean fireAllowed = true;
    private boolean entityGriefingAllowed = false;
    private boolean redstoneAllowed = true;
    private boolean containerAccessAllowed = true;
    private boolean animalInteractAllowed = true;
    private boolean bucketUsageAllowed = true;
    private boolean itemFramesAllowed = true;
    private boolean vehiclesAllowed = true;

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean enabled) { this.pvpEnabled = enabled; }

    public boolean isKeepItemsEnabled() { return keepItemsEnabled; }
    public void setKeepItemsEnabled(boolean enabled) { this.keepItemsEnabled = enabled; }

    public boolean isExplosionsAllowed() { return explosionsAllowed; }
    public void setExplosionsAllowed(boolean allowed) { this.explosionsAllowed = allowed; }

    public boolean isFireAllowed() { return fireAllowed; }
    public void setFireAllowed(boolean allowed) { this.fireAllowed = allowed; }

    public boolean isEntityGriefingAllowed() { return entityGriefingAllowed; }
    public void setEntityGriefingAllowed(boolean allowed) { this.entityGriefingAllowed = allowed; }

    public boolean isRedstoneAllowed() { return redstoneAllowed; }
    public void setRedstoneAllowed(boolean allowed) { this.redstoneAllowed = allowed; }

    public boolean isContainerAccessAllowed() { return containerAccessAllowed; }
    public void setContainerAccessAllowed(boolean allowed) { this.containerAccessAllowed = allowed; }

    public boolean isAnimalInteractAllowed() { return animalInteractAllowed; }
    public void setAnimalInteractAllowed(boolean allowed) { this.animalInteractAllowed = allowed; }

    public boolean isBucketsAllowed() { return bucketUsageAllowed; }
    public void setBucketsAllowed(boolean allowed) { this.bucketUsageAllowed = allowed; }

    public boolean isItemFramesAllowed() { return itemFramesAllowed; }
    public void setItemFramesAllowed(boolean allowed) { this.itemFramesAllowed = allowed; }

    public boolean isVehiclesAllowed() { return vehiclesAllowed; }
    public void setVehiclesAllowed(boolean allowed) { this.vehiclesAllowed = allowed; }

    /**
     * Serialize claim settings into map for storage.
     */
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("pvp", pvpEnabled);
        map.put("keep-items", keepItemsEnabled);
        map.put("explosions", explosionsAllowed);
        map.put("fire", fireAllowed);
        map.put("entity-grief", entityGriefingAllowed);
        map.put("redstone", redstoneAllowed);
        map.put("container", containerAccessAllowed);
        map.put("animals", animalInteractAllowed);
        map.put("buckets", bucketUsageAllowed);
        map.put("item-frames", itemFramesAllowed);
        map.put("vehicles", vehiclesAllowed);
        return map;
    }

    /**
     * Deserialize from map to settings object.
     */
    public static PlotSettings deserialize(Map<String, Object> map) {
        PlotSettings settings = new PlotSettings();
        if (map == null) return settings;

        settings.pvpEnabled = (boolean) map.getOrDefault("pvp", false);
        settings.keepItemsEnabled = (boolean) map.getOrDefault("keep-items", false);
        settings.explosionsAllowed = (boolean) map.getOrDefault("explosions", true);
        settings.fireAllowed = (boolean) map.getOrDefault("fire", true);
        settings.entityGriefingAllowed = (boolean) map.getOrDefault("entity-grief", false);
        settings.redstoneAllowed = (boolean) map.getOrDefault("redstone", true);
        settings.containerAccessAllowed = (boolean) map.getOrDefault("container", true);
        settings.animalInteractAllowed = (boolean) map.getOrDefault("animals", true);
        settings.bucketUsageAllowed = (boolean) map.getOrDefault("buckets", true);
        settings.itemFramesAllowed = (boolean) map.getOrDefault("item-frames", true);
        settings.vehiclesAllowed = (boolean) map.getOrDefault("vehicles", true);

        return settings;
    }
}
