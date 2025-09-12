// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.*;

/**
 * ClaimRoleManager
 * - Handles roles inside claims
 * - Supports trust/untrust, transfer, and permission checks
 *
 * Consolidated for v1.2.5+
 */
public class ClaimRoleManager {

    private final PlotManager plotManager;

    // Maps: plotId -> {playerName -> roleName}
    private final Map<UUID, Map<String, String>> roleCache = new HashMap<>();

    public ClaimRoleManager(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    /* ======================================================
     * TRUST / UNTRUST
     * ====================================================== */
    public boolean trustPlayer(Plot plot, String playerName, String role) {
        UUID plotId = plot.getId();
        Map<String, String> map = roleCache.computeIfAbsent(plotId, k -> new HashMap<>());

        if (map.containsKey(playerName)) return false;
        map.put(playerName, role.toLowerCase(Locale.ROOT));
        return true;
    }

    public boolean untrustPlayer(Plot plot, String playerName) {
        UUID plotId = plot.getId();
        Map<String, String> map = roleCache.get(plotId);

        if (map == null || !map.containsKey(playerName)) return false;
        map.remove(playerName);
        return true;
    }

    public boolean transferOwnership(Plot plot, String newOwnerName) {
        // Prevent transferring to same player
        if (plot.getOwnerName().equalsIgnoreCase(newOwnerName)) return false;

        // Update plot owner
        plot.setOwnerName(newOwnerName);

        // Clear cached roles (fresh start for new owner)
        roleCache.remove(plot.getId());
        return true;
    }

    /* ======================================================
     * ROLE QUERIES
     * ====================================================== */
    public String getRole(UUID plotId, String playerName) {
        Map<String, String> map = roleCache.get(plotId);
        if (map == null) return null;
        return map.get(playerName);
    }

    public boolean isTrusted(UUID plotId, String playerName) {
        Map<String, String> map = roleCache.get(plotId);
        return map != null && map.containsKey(playerName);
    }

    /* ======================================================
     * PERMISSION CHECKS
     * ====================================================== */

    /** Build & destroy blocks, buckets, vehicles */
    public boolean canManage(UUID playerId, UUID plotId) {
        return hasRole(playerId, plotId, Set.of("owner", "co-owner", "builder"));
    }

    /** Doors, levers, buttons, crops */
    public boolean canInteract(UUID playerId, UUID plotId) {
        return hasRole(playerId, plotId, Set.of("owner", "co-owner", "builder", "trusted"));
    }

    /** Containers: chests, hoppers, furnaces, shulkers */
    public boolean canContainers(UUID playerId, UUID plotId) {
        return hasRole(playerId, plotId, Set.of("owner", "co-owner", "builder"));
    }

    /** Special case: allow unclaiming */
    public boolean canUnclaim(UUID playerId, UUID plotId) {
        return hasRole(playerId, plotId, Set.of("owner", "co-owner"));
    }

    /* ======================================================
     * INTERNAL HELPERS
     * ====================================================== */
    private boolean hasRole(UUID playerId, UUID plotId, Set<String> allowed) {
        Plot plot = plotManager.getPlot(plotId);
        if (plot == null) return false;

        // Direct owner check
        if (plot.isOwner(playerId)) return true;

        // Look up cached role by name
        String playerName = plotManager.getPlayerName(playerId);
        if (playerName == null) return false;

        String role = getRole(plotId, playerName);
        return role != null && allowed.contains(role.toLowerCase(Locale.ROOT));
    }
}
