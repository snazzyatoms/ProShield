// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;

import java.util.*;

/**
 * Manages role assignments within claims.
 * Combines role mapping + permission checks into one class.
 */
public class ClaimRoleManager {

    // Map<claimId, Map<playerId, role>>
    private final Map<UUID, Map<UUID, ClaimRole>> claimRoles = new HashMap<>();

    /**
     * Get the role of a player in a claim.
     */
    public ClaimRole getRole(UUID claimId, UUID playerId) {
        return claimRoles
                .getOrDefault(claimId, Collections.emptyMap())
                .getOrDefault(playerId, ClaimRole.NONE);
    }

    /**
     * Assign a role to a player in a claim.
     */
    public void setRole(UUID claimId, UUID playerId, ClaimRole role) {
        claimRoles
                .computeIfAbsent(claimId, id -> new HashMap<>())
                .put(playerId, role);
    }

    /**
     * Remove a player's role in a claim.
     */
    public void removeRole(UUID claimId, UUID playerId) {
        Map<UUID, ClaimRole> map = claimRoles.get(claimId);
        if (map != null) {
            map.remove(playerId);
            if (map.isEmpty()) {
                claimRoles.remove(claimId);
            }
        }
    }

    /**
     * Get all players with roles in a claim.
     */
    public Map<UUID, ClaimRole> getAllRoles(UUID claimId) {
        return claimRoles.getOrDefault(claimId, Collections.emptyMap());
    }

    /* ----------------------------------------------------
     * Permission checks (merged from RolePermissions.java)
     * ---------------------------------------------------- */

    public boolean canBuild(UUID claimId, UUID playerId) {
        return getRole(claimId, playerId).canBuild();
    }

    public boolean canContainers(UUID claimId, UUID playerId) {
        return getRole(claimId, playerId).canContainers();
    }

    public boolean canManageTrust(UUID claimId, UUID playerId) {
        return getRole(claimId, playerId).canManageTrust();
    }

    public boolean canUnclaim(UUID claimId, UUID playerId) {
        return getRole(claimId, playerId).canUnclaim();
    }

    public boolean canFlags(UUID claimId, UUID playerId) {
        return getRole(claimId, playerId).canFlags();
    }

    public boolean canManageRoles(UUID claimId, UUID playerId) {
        return getRole(claimId, playerId).canManageRoles();
    }

    /**
     * Reset all roles for a claim (e.g., on unclaim).
     */
    public void clearRoles(UUID claimId) {
        claimRoles.remove(claimId);
    }
}
