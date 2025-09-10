package com.snazzyatoms.proshield.plots;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores per-plot flags/settings (PvP, explosions, fire, redstone, etc).
 */
public class PlotSettings {

    private final Map<String, Boolean> flags = new HashMap<>();

    public PlotSettings() {
        // Default flags (should sync with config.yml defaults)
        flags.put("pvp", false);
        flags.put("explosions", false);
        flags.put("fire", false);
        flags.put("redstone", true);
        flags.put("containers", true);
        flags.put("animal-access", true);
        flags.put("entity-grief", false);
        flags.put("buckets", false);
        flags.put("keep-items", false);
        flags.put("item-frames", false);
        flags.put("vehicles", false);
    }

    public boolean isFlagEnabled(String flag) {
        return flags.getOrDefault(flag, false);
    }

    public void setFlag(String flag, boolean value) {
        flags.put(flag, value);
    }

    // Convenience wrappers
    public boolean isPvpEnabled() { return isFlagEnabled("pvp"); }
    public boolean isExplosionsEnabled() { return isFlagEnabled("explosions"); }
    public boolean isFireEnabled() { return isFlagEnabled("fire"); }
    public boolean isRedstoneEnabled() { return isFlagEnabled("redstone"); }
    public boolean isContainersEnabled() { return isFlagEnabled("containers"); }
    public boolean isAnimalAccessEnabled() { return isFlagEnabled("animal-access"); }
    public boolean isEntityGriefingAllowed() { return isFlagEnabled("entity-grief"); }
    public boolean isBucketsAllowed() { return isFlagEnabled("buckets"); }
    public boolean isKeepItemsEnabled() { return isFlagEnabled("keep-items"); }
    public boolean isItemFramesAllowed() { return isFlagEnabled("item-frames"); }
    public boolean isVehiclesAllowed() { return isFlagEnabled("vehicles"); }
}
