// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;

import java.util.Locale;
import java.util.UUID;

/**
 * ClaimRoleManager (v1.2.6 FINAL-PATCHED2)
 *
 * - Centralized manager for role assignment inside claims
 * - Provides default role fallbacks
 * - Converts between stored String values in Plot and ClaimRole enum
 * - Used by GUIManager + ClaimProtectionListener
 */
public class ClaimRoleManager {

    private final ProShield plugin;

    /**
     * Primary constructor — preferred.
     */
    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Fallback no-arg constructor (uses ProShield.getInstance()).
     */
    public ClaimRoleManager() {
        this.plugin = ProShield.getInstance();
    }

    /* -------------------------
     * Role Assignment
     * ------------------------- */

    /**
     * Assign a role to a player in a claim.
     *
     * @param plot   Target claim
     * @param player Target player UUID
     * @param role   Role to assign
     */
    public void setRole(Plot plot, UUID player, ClaimRole role) {
        if (plot == null || player == null || role == null) return;
        // ✅ Always store as lowercase string id
        plot.getTrusted().put(player, role.getId());
    }

    /**
     * Get a player's role in a claim.
     *
     * @param plot   Target claim
     * @param player Player UUID
     * @return ClaimRole (defaults to ClaimRole.NONE if not found)
     */
    public ClaimRole getRole(Plot plot, UUID player) {
        if (plot == null || player == null) return ClaimRole.NONE;
        String stored = plot.getTrusted().get(player);
        if (stored == null || stored.isBlank()) return ClaimRole.NONE;
        return ClaimRole.fromName(stored);
    }

    /**
     * Remove a player's role from a claim.
     */
    public void removeRole(Plot plot, UUID player) {
        if (plot == null || player == null) return;
        plot.getTrusted().remove(player);
    }

    /* -------------------------
     * Defaults & Utility
     * ------------------------- */

    /**
     * Get the default role ID (string) for new trusted players.
     */
    public String getDefaultRoleId() {
        return plugin != null
                ? plugin.getConfig().getString("roles.default", "member").toLowerCase(Locale.ROOT)
                : "member";
    }

    /**
     * Get the default ClaimRole (as enum).
     */
    public ClaimRole getDefaultRole() {
        return ClaimRole.fromName(getDefaultRoleId());
    }

    /**
     * Get the raw stored role ID for a player in a claim.
     */
    public String getRoleId(Plot plot, UUID player) {
        if (plot == null || player == null) return "none";
        String stored = plot.getTrusted().get(player);
        return (stored != null ? stored.toLowerCase(Locale.ROOT) : "none");
    }
}
