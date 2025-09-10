package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages claim roles (permissions inside claims).
 */
public class ClaimRoleManager {

    // Stores player roles per-claim
    private final Map<UUID, Map<UUID, ClaimRole>> claimRoles = new HashMap<>();

    /**
     * Get a player’s role inside a claim.
     */
    public ClaimRole getRole(Plot plot, Player player) {
        if (plot == null) return ClaimRole.VISITOR;
        if (plot.getOwner().equals(player.getUniqueId())) return ClaimRole.OWNER;

        return claimRoles
                .getOrDefault(plot.getId(), new HashMap<>())
                .getOrDefault(player.getUniqueId(), ClaimRole.VISITOR);
    }

    /**
     * Assign a role to a player in a claim.
     */
    public void setRole(Plot plot, Player player, ClaimRole role) {
        claimRoles
                .computeIfAbsent(plot.getId(), k -> new HashMap<>())
                .put(player.getUniqueId(), role);
    }

    /**
     * Permission checks by role.
     */
    public boolean canBuild(ClaimRole role) {
        return role.isAtLeast(ClaimRole.BUILDER);
    }

    public boolean canUseContainers(ClaimRole role) {
        return role.isAtLeast(ClaimRole.CONTAINER);
    }

    public boolean canInteract(ClaimRole role) {
        return role.isAtLeast(ClaimRole.MEMBER);
    }

    public boolean isOwnerOrCoOwner(ClaimRole role) {
        return role == ClaimRole.OWNER || role == ClaimRole.CO_OWNER;
    }
}
