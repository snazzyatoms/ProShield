// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;

import java.util.Map;
import java.util.UUID;

public class ClaimRoleManager {

    private final ProShield plugin;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public void reloadFromConfig() {
        // currently stateless; reserved for future role configs
    }

    public ClaimRole getRole(Plot plot, UUID player) {
        if (plot == null || player == null) return ClaimRole.VISITOR;
        if (plot.isOwner(player)) return ClaimRole.COOWNER; // owner treated as top role for checks
        Map<UUID, ClaimRole> t = plot.getTrusted();
        return t.getOrDefault(player, ClaimRole.VISITOR);
    }

    public boolean isOwnerOrCoOwner(ClaimRole role) {
        return role != null && role.atLeast(ClaimRole.COOWNER);
    }

    public boolean canBuild(ClaimRole role) {
        return role != null && role.atLeast(ClaimRole.BUILDER);
    }

    public boolean canInteract(ClaimRole role) {
        return role != null && role.atLeast(ClaimRole.MEMBER);
    }

    public void trustPlayer(Plot plot, String playerName, ClaimRole role) {
        if (plot == null || playerName == null || role == null) return;
        UUID uuid = plugin.getServer().getOfflinePlayer(playerName).getUniqueId();
        plot.addTrusted(uuid, role);
    }

    public void untrustPlayer(Plot plot, String playerName) {
        if (plot == null || playerName == null) return;
        plot.removeTrusted(playerName);
    }
}
