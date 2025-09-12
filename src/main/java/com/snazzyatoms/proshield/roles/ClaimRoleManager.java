// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Locale;
import java.util.UUID;

public class ClaimRoleManager {

    private final com.snazzyatoms.proshield.plots.PlotManager plotManager;

    public ClaimRoleManager(com.snazzyatoms.proshield.plots.PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    public boolean canUnclaim(UUID playerId, UUID plotId) {
        Plot plot = plotManager.getPlotById(plotId);
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;
        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canUnclaim();
    }

    public boolean trustPlayer(Plot plot, String targetName, String roleName) {
        if (plot == null || targetName == null || targetName.isEmpty()) return false;

        OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
        UUID targetId = op.getUniqueId();
        ClaimRole role = parseRole(roleName);
        if (role == null) return false;

        plot.assignRole(targetId, role);
        return true;
    }

    public boolean untrustPlayer(Plot plot, String targetName) {
        if (plot == null || targetName == null || targetName.isEmpty()) return false;
        OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
        UUID targetId = op.getUniqueId();
        plot.clearRole(targetId);
        return true;
    }

    public boolean transferOwnership(Plot plot, String targetName) {
        if (plot == null || targetName == null || targetName.isEmpty()) return false;
        OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
        if (op == null) return false;
        plot.setOwner(op.getUniqueId());
        return true;
    }

    public ClaimRole getRole(UUID claimId, UUID playerId) {
        Plot plot = plotManager.getPlotById(claimId);
        return (plot != null) ? plot.getRole(playerId) : null;
    }

    private ClaimRole parseRole(String name) {
        if (name == null) return ClaimRole.TRUSTED;
        switch (name.toLowerCase(Locale.ROOT)) {
            case "visitor" -> { return ClaimRole.VISITOR; }
            case "trusted" -> { return ClaimRole.TRUSTED; }
            case "builder" -> { return ClaimRole.BUILDER; }
            case "container" -> { return ClaimRole.CONTAINER; }
            case "moderator" -> { return ClaimRole.MODERATOR; }
            case "manager" -> { return ClaimRole.MANAGER; }
            case "owner" -> { return ClaimRole.OWNER; }
            default -> { return null; }
        }
    }

    // For roles GUI in future (kept simple)
    public RolePermissions getRolePermissions(UUID claimId, String key) {
        return new RolePermissions(); // stub with defaults enabled/disabled as your impl requires
    }
}
