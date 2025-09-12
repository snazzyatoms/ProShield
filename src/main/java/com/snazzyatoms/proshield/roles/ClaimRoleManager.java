// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.*;

public class ClaimRoleManager {

    private final PlotManager plotManager;

    // Per-claim role permission overrides (UUID = claimId, String = roleId)
    private final Map<UUID, Map<String, RolePermissions>> rolePermissions = new HashMap<>();

    public ClaimRoleManager(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    /* -------------------------------------------------------
     * Core Role Logic
     * ------------------------------------------------------- */

    public boolean canBuild(UUID playerId, Plot plot) {
        if (plot == null) return true; // wilderness
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getTrusted().get(playerId);
        if (role == null) return false;

        RolePermissions perms = getRolePermissions(plot.getId(), role.name().toLowerCase());
        return perms.canBuild();
    }

    public boolean canInteract(UUID playerId, Plot plot) {
        if (plot == null) return true;
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getTrusted().get(playerId);
        if (role == null) return false;

        RolePermissions perms = getRolePermissions(plot.getId(), role.name().toLowerCase());
        return perms.canInteract();
    }

    public boolean canManageTrust(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getTrusted().get(playerId);
        if (role == null) return false;

        RolePermissions perms = getRolePermissions(claimId, role.name().toLowerCase());
        return perms.canManageTrust();
    }

    public boolean canUnclaim(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getTrusted().get(playerId);
        if (role == null) return false;

        RolePermissions perms = getRolePermissions(claimId, role.name().toLowerCase());
        return perms.canUnclaim();
    }

    public boolean isOwnerOrCoOwner(UUID playerId, Plot plot) {
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getTrusted().get(playerId);
        return role == ClaimRole.MANAGER; // treat Manager as co-owner
    }

    /* -------------------------------------------------------
     * Trust / Untrust
     * ------------------------------------------------------- */

    public void trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        plot.getTrusted().put(playerId, role);
        plotManager.saveAsync(plot);
    }

    public void untrustPlayer(Plot plot, UUID playerId) {
        plot.getTrusted().remove(playerId);
        plotManager.saveAsync(plot);
    }

    /* -------------------------------------------------------
     * Role Management
     * ------------------------------------------------------- */

    public void setRole(Plot plot, UUID playerId, ClaimRole role) {
        if (plot.isOwner(playerId)) return; // never override owner
        plot.getTrusted().put(playerId, role);
        plotManager.saveAsync(plot);
    }

    /** Assign role via GUI/commands. */
    public void assignRole(UUID claimId, UUID playerId, String roleName) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return;

        ClaimRole role = ClaimRole.fromString(roleName);
        if (role == null) role = ClaimRole.TRUSTED;

        plot.getTrusted().put(playerId, role);
        plotManager.saveAsync(plot);
    }

    /** Clear role for a player (reverts to none/trusted). */
    public void clearRole(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return;

        plot.getTrusted().remove(playerId);
        plotManager.saveAsync(plot);
    }

    /** Get role name for GUIManager (string-based). */
    public String getRole(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return "None";
        if (plot.isOwner(playerId)) return "Owner";

        ClaimRole role = plot.getTrusted().get(playerId);
        return (role != null) ? role.name() : "Trusted";
    }

    /** Get raw ClaimRole enum. */
    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot.isOwner(playerId)) return ClaimRole.OWNER;
        return plot.getTrusted().getOrDefault(playerId, ClaimRole.VISITOR);
    }

    public void clearAllRoles(Plot plot) {
        plot.getTrusted().clear();
        plotManager.saveAsync(plot);
    }

    /* -------------------------------------------------------
     * Role Permissions
     * ------------------------------------------------------- */

    public RolePermissions getRolePermissions(UUID claimId, String roleId) {
        rolePermissions.putIfAbsent(claimId, new HashMap<>());
        Map<String, RolePermissions> map = rolePermissions.get(claimId);
        return map.computeIfAbsent(roleId.toLowerCase(), k -> new RolePermissions());
    }

    public void setRolePermissions(UUID claimId, String roleId, RolePermissions perms) {
        rolePermissions.putIfAbsent(claimId, new HashMap<>());
        rolePermissions.get(claimId).put(roleId.toLowerCase(), perms);
    }
}
