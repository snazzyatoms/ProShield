// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;

import java.util.Locale;
import java.util.UUID;

/**
 * ClaimRoleManager (v1.2.6 FINAL-SYNCED)
 *
 * - Stores roles in Plot.trusted as lowercase string IDs
 * - Converts safely between ClaimRole enum and stored IDs
 * - Provides default role fallback
 * - Used by GUIManager + ClaimProtectionListener
 */
public class ClaimRoleManager {

    private final ProShield plugin;

    /** Preferred constructor */
    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /** Fallback constructor (uses ProShield.getInstance()) */
    public ClaimRoleManager() {
        this.plugin = ProShield.getInstance();
    }

    /* -------------------------
     * Role Assignment
     * ------------------------- */

    /** Assign a role to a player in a claim. */
    public void setRole(Plot plot, UUID player, ClaimRole role) {
        if (plot == null || player == null || role == null) return;
        // Store role ID as string (lowercase)
        plot.getTrusted().put(player, role.getId());
    }

    /** Get a player’s ClaimRole from a claim. */
    public ClaimRole getRole(Plot plot, UUID player) {
        if (plot == null || player == null) return ClaimRole.NONE;
        String stored = plot.getTrusted().get(player);
        if (stored == null || stored.isBlank()) return ClaimRole.NONE;
        return ClaimRole.fromName(stored);
    }

    /** Remove a player from trusted map. */
    public void removeRole(Plot plot, UUID player) {
        if (plot == null || player == null) return;
        plot.getTrusted().remove(player);
    }

    /* -------------------------
     * Defaults & Utility
     * ------------------------- */

    /** Get default role ID for new trusted players (string). */
    public String getDefaultRoleId() {
        return plugin != null
                ? plugin.getConfig().getString("roles.default", "member").toLowerCase(Locale.ROOT)
                : "member";
    }

    /** Get default ClaimRole (enum). */
    public ClaimRole getDefaultRole() {
        return ClaimRole.fromName(getDefaultRoleId());
    }

    /** Get stored role ID for a player in a claim (string). */
    public String getRoleId(Plot plot, UUID player) {
        if (plot == null || player == null) return "none";
        String stored = plot.getTrusted().get(player);
        return (stored != null ? stored.toLowerCase(Locale.ROOT) : "none");
    }
}
