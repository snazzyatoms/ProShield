package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.roles.ClaimRole;
import java.util.EnumMap;
import java.util.Map;

/**
 * Stores per-claim settings (PvP, item rules, keep-drops, etc.).
 * These settings override global defaults if enabled in config.yml.
 */
public class PlotSettings {

    // === Per-claim flags ===
    private boolean pvpEnabled = false;
    private boolean keepDropsEnabled = false;

    // Role-based item permissions
    private final Map<ClaimRole, Boolean> dropPermissions = new EnumMap<>(ClaimRole.class);
    private final Map<ClaimRole, Boolean> pickupPermissions = new EnumMap<>(ClaimRole.class);

    public PlotSettings() {
        // Default: only owner/co-owner can drop & pick up if global allows
        for (ClaimRole role : ClaimRole.values()) {
            dropPermissions.put(role, role == ClaimRole.OWNER || role == ClaimRole.CO_OWNER);
            pickupPermissions.put(role, role == ClaimRole.OWNER || role == ClaimRole.CO_OWNER);
        }
    }

    // === PvP ===
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    // === Keep Drops ===
    public boolean isKeepDropsEnabled() {
        return keepDropsEnabled;
    }

    public void setKeepDropsEnabled(boolean keepDropsEnabled) {
        this.keepDropsEnabled = keepDropsEnabled;
    }

    // === Item Drop Rules ===
    public boolean canDropItems(ClaimRole role) {
        return dropPermissions.getOrDefault(role, false);
    }

    public void setDropPermission(ClaimRole role, boolean allowed) {
        dropPermissions.put(role, allowed);
    }

    // === Item Pickup Rules ===
    public boolean canPickupItems(ClaimRole role) {
        return pickupPermissions.getOrDefault(role, false);
    }

    public void setPickupPermission(ClaimRole role, boolean allowed) {
        pickupPermissions.put(role, allowed);
    }

    // === Utility ===
    public void resetToDefaults() {
        pvpEnabled = false;
        keepDropsEnabled = false;
        for (ClaimRole role : ClaimRole.values()) {
            dropPermissions.put(role, role == ClaimRole.OWNER || role == ClaimRole.CO_OWNER);
            pickupPermissions.put(role, role == ClaimRole.OWNER || role == ClaimRole.CO_OWNER);
        }
    }
}
