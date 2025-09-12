// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClaimRoleManager
 * - Handles trust/untrust of players in claims
 * - Stores and resolves role permissions
 * - Provides utility for ownership transfer
 * - Now includes dynamic role list for tab completion
 *
 * Consolidated for v1.2.5
 */
public class ClaimRoleManager {

    // Map<claimId, Map<playerUUID, roleName>>
    private final Map<UUID, Map<UUID, String>> claimRoles = new ConcurrentHashMap<>();

    /**
     * Trust a player with a role in a claim.
     */
    public boolean trustPlayer(Plot plot, String playerName, String role) {
        if (plot == null || playerName == null) return false;

        UUID claimId = plot.getId();
        UUID playerId = resolveUUID(playerName);
        if (playerId == null) return false;

        claimRoles.computeIfAbsent(claimId, k -> new ConcurrentHashMap<>())
                  .put(playerId, role.toLowerCase(Locale.ROOT));
        plot.addTrusted(playerId, playerName); // keep backward compatibility
        return true;
    }

    /**
     * Untrust a player in a claim.
     */
    public boolean untrustPlayer(Plot plot, String playerName) {
        if (plot == null || playerName == null) return false;

        UUID claimId = plot.getId();
        UUID playerId = resolveUUID(playerName);
        if (playerId == null) return false;

        Map<UUID, String> roles = claimRoles.get(claimId);
        if (roles != null) {
            roles.remove(playerId);
        }
        plot.removeTrusted(playerId); // maintain old behavior
        return true;
    }

    /**
     * Check if a player can unclaim land in a plot.
     */
    public boolean canUnclaim(UUID playerId, UUID claimId) {
        if (playerId == null || claimId == null) return false;
        String role = getRole(claimId, playerId);
        return role != null && (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("admin"));
    }

    /**
     * Check if a player can manage (trust/untrust/roles).
     */
    public boolean canManage(UUID playerId, UUID claimId) {
        if (playerId == null || claimId == null) return false;
        String role = getRole(claimId, playerId);
        return role != null && (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("moderator"));
    }

    /**
     * Transfer claim ownership to another player.
     */
    public boolean transferOwnership(Plot plot, String targetName) {
        if (plot == null || targetName == null) return false;

        UUID newOwnerId = resolveUUID(targetName);
        if (newOwnerId == null) return false;

        plot.setOwner(newOwnerId);
        return true;
    }

    /**
     * Get role assigned to a player in a claim.
     */
    public String getRole(UUID claimId, UUID playerId) {
        Map<UUID, String> roles = claimRoles.get(claimId);
        return (roles != null) ? roles.get(playerId) : null;
    }

    /**
     * Return a dynamic list of available roles (for tab completion).
     * This could be driven by config.yml in the future.
     */
    public List<String> getAvailableRoles() {
        return Arrays.asList("trusted", "builder", "container", "moderator", "manager");
    }

    /**
     * Resolve a player name to UUID.
     * Currently placeholder — in production this should query Bukkit.getOfflinePlayer.
     */
    private UUID resolveUUID(String name) {
        try {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
        } catch (Exception e) {
            return null;
        }
    }
}
