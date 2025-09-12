// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;

import java.util.*;

/**
 * Manages claim roles, trusted players, and ownership transfers.
 * Consolidated from v1.2.0 → v1.2.5 with helper methods for tab completion.
 */
public class ClaimRoleManager {

    // Map: plotId -> (playerName -> role)
    private final Map<UUID, Map<String, ClaimRole>> claimRoles = new HashMap<>();

    /* ======================================================
     * Trust / Untrust
     * ====================================================== */

    public boolean trustPlayer(Plot plot, String playerName, String roleName) {
        ClaimRole role = ClaimRole.fromName(roleName);
        if (role == null || role == ClaimRole.NONE) {
            return false;
        }

        claimRoles.computeIfAbsent(plot.getId(), k -> new HashMap<>())
                  .put(playerName.toLowerCase(Locale.ROOT), role);
        return true;
    }

    public boolean untrustPlayer(Plot plot, String playerName) {
        Map<String, ClaimRole> roles = claimRoles.get(plot.getId());
        if (roles == null) return false;

        return roles.remove(playerName.toLowerCase(Locale.ROOT)) != null;
    }

    public ClaimRole getRole(Plot plot, String playerName) {
        Map<String, ClaimRole> roles = claimRoles.get(plot.getId());
        if (roles == null) return ClaimRole.NONE;

        return roles.getOrDefault(playerName.toLowerCase(Locale.ROOT), ClaimRole.NONE);
    }

    public boolean isTrusted(Plot plot, String playerName) {
        return getRole(plot, playerName) != ClaimRole.NONE;
    }

    /* ======================================================
     * Ownership
     * ====================================================== */

    public boolean transferOwnership(Plot plot, String newOwner) {
        if (plot == null) return false;

        UUID oldOwner = plot.getOwner();
        if (oldOwner == null) return false;

        // Reassign
        plot.setOwnerName(newOwner);
        return true;
    }

    public boolean canUnclaim(UUID playerId, UUID plotId) {
        // For now, only owners can unclaim. Could extend later.
        return false;
    }

    /* ======================================================
     * Roles List (for Tab Completion)
     * ====================================================== */

    public List<String> getAllRoles() {
        List<String> roles = new ArrayList<>();
        for (ClaimRole role : ClaimRole.values()) {
            if (role != ClaimRole.NONE && role != ClaimRole.VISITOR) {
                roles.add(role.name().toLowerCase(Locale.ROOT));
            }
        }
        return roles;
    }
}
