// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;

import java.util.Locale;
import java.util.UUID;

/**
 * ClaimRoleManager
 * - Central API for managing trusted players' roles inside claims
 * - Provides conversion between stored strings and ClaimRole enum
 * - Used by GUIManager and command handlers
 */
public class ClaimRoleManager {

    private final ProShield plugin;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* -------------------------
     * ROLE API
     * ------------------------- */

    /**
     * Get the role of a player inside a given plot.
     *
     * @param plot   The claim/plot
     * @param player The player's UUID
     * @return ClaimRole (defaults to NONE if not trusted or invalid)
     */
    public ClaimRole getRole(Plot plot, UUID player) {
        if (plot == null || player == null) return ClaimRole.NONE;
        String raw = plot.getTrusted().get(player);
        return ClaimRole.fromName(raw);
    }

    /**
     * Set the role of a player inside a given plot.
     *
     * @param plot   The claim/plot
     * @param player The player's UUID
     * @param role   The role to assign
     */
    public void setRole(Plot plot, UUID player, ClaimRole role) {
        if (plot == null || player == null || role == null) return;
        plot.getTrusted().put(player, role.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Remove a trusted player from a given plot.
     *
     * @param plot   The claim/plot
     * @param player The player's UUID
     */
    public void removeTrusted(Plot plot, UUID player) {
        if (plot == null || player == null) return;
        plot.getTrusted().remove(player);
    }

    /**
     * Check if a player has at least the given role inside a plot.
     *
     * @param plot   The claim/plot
     * @param player The player's UUID
     * @param needed The required minimum role
     * @return true if player has >= role
     */
    public boolean hasRoleAtLeast(Plot plot, UUID player, ClaimRole needed) {
        if (plot == null || player == null || needed == null) return false;
        ClaimRole current = getRole(plot, player);
        return current.ordinal() >= needed.ordinal();
    }

    /* -------------------------
     * DEBUG / LOGGING
     * ------------------------- */
    public void debugRoles(Plot plot) {
        if (plot == null) return;
        plugin.getLogger().info("[ClaimRoleManager] Roles for plot " + plot.getId() + ": " + plot.getTrusted());
    }
}
