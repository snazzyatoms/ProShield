package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.*;

public class ClaimRoleManager {

    private final PlotManager plotManager;

    // per-claim role permission overrides
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

    public boolean isOwnerOrCoOwner(UUID playerId, Plot plot) {
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getTrusted().get(playerId);
        return role == ClaimRole.CO_OWNER;
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

    /** Return role name for GUIManager (string-based). */
    public String getRole(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return "None";
        if (plot.isOwner(playerId)) return "Owner";

        ClaimRole role = plot.getTrusted().get(playerId);
        return (role != null) ? role.name() : "Trusted";
    }

    /** Return raw ClaimRole enum (for backend use). */
    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot.isOwner(playerId)) return ClaimRole.OWNER;
        return plot.getTrusted().getOrDefault(playerId, ClaimRole.NONE);
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
