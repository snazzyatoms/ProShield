package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;

import java.util.*;

public class ClaimRoleManager {

    private final PlotManager plotManager;
    private final Map<UUID, Map<String, RolePermissions>> rolePermissions = new HashMap<>();

    public ClaimRoleManager(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    /* -------------------- Permissions -------------------- */

    public boolean canBuild(UUID playerId, Plot plot) {
        if (plot == null) return true;
        if (plot.isOwner(playerId)) return true;
        ClaimRole role = plot.getTrusted().get(playerId);
        if (role == null) return false;
        return getRolePermissions(plot.getId(), role.name().toLowerCase()).canBuild();
    }

    public boolean canInteract(UUID playerId, Plot plot) {
        if (plot == null) return true;
        if (plot.isOwner(playerId)) return true;
        ClaimRole role = plot.getTrusted().get(playerId);
        if (role == null) return false;
        return getRolePermissions(plot.getId(), role.name().toLowerCase()).canInteract();
    }

    public boolean canManageTrust(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;
        ClaimRole role = plot.getTrusted().get(playerId);
        return role != null && getRolePermissions(claimId, role.name().toLowerCase()).canManageTrust();
    }

    public boolean canUnclaim(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;
        ClaimRole role = plot.getTrusted().get(playerId);
        return role != null && getRolePermissions(claimId, role.name().toLowerCase()).canUnclaim();
    }

    public boolean isOwnerOrManager(UUID playerId, Plot plot) {
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;
        ClaimRole role = plot.getTrusted().get(playerId);
        return role == ClaimRole.MANAGER;
    }

    /* -------------------- Trust -------------------- */

    public void trustPlayer(Plot plot, UUID playerId, ClaimRole role) {
        plot.getTrusted().put(playerId, role);
        plotManager.saveAsync(plot);
    }

    public void untrustPlayer(Plot plot, UUID playerId) {
        plot.getTrusted().remove(playerId);
        plotManager.saveAsync(plot);
    }

    /* -------------------- Roles -------------------- */

    public void setRole(Plot plot, UUID playerId, ClaimRole role) {
        if (plot.isOwner(playerId)) return;
        plot.getTrusted().put(playerId, role);
        plotManager.saveAsync(plot);
    }

    public ClaimRole getRole(Plot plot, UUID playerId) {
        if (plot.isOwner(playerId)) return ClaimRole.OWNER;
        return plot.getTrusted().getOrDefault(playerId, ClaimRole.VISITOR);
    }

    /* -------------------- Permissions storage -------------------- */

    public RolePermissions getRolePermissions(UUID claimId, String roleId) {
        rolePermissions.putIfAbsent(claimId, new HashMap<>());
        return rolePermissions.get(claimId)
                .computeIfAbsent(roleId.toLowerCase(), k -> RolePermissions.defaultsFor(roleId));
    }

    public void setRolePermissions(UUID claimId, String roleId, RolePermissions perms) {
        rolePermissions.putIfAbsent(claimId, new HashMap<>());
        rolePermissions.get(claimId).put(roleId.toLowerCase(), perms);
    }
}
