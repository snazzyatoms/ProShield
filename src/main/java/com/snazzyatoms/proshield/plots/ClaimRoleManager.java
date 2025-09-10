// path: src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Centralized role resolution + capability checks.
 * Preserves previous semantics and adds helpers for new flags.
 */
public class ClaimRoleManager {

    private final ProShield plugin;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /** Resolve effective role of player inside a plot. */
    public ClaimRole getRole(Plot plot, Player player) {
        if (plot == null || player == null) return ClaimRole.VISITOR;
        UUID owner = plot.getOwner();
        if (owner != null && owner.equals(player.getUniqueId())) {
            return ClaimRole.OWNER;
        }
        Map<UUID, String> trusted = plot.getTrusted(); // role names stored as strings
        if (trusted != null) {
            String roleName = trusted.get(player.getUniqueId());
            if (roleName != null) {
                return ClaimRole.fromString(roleName, ClaimRole.MEMBER);
            }
        }
        return ClaimRole.VISITOR;
    }

    public boolean isOwnerOrCoOwner(ClaimRole role) {
        return role == ClaimRole.OWNER || role == ClaimRole.CO_OWNER;
    }

    public boolean canBuild(ClaimRole role) {
        return role == ClaimRole.BUILDER || role == ClaimRole.CO_OWNER || role == ClaimRole.OWNER;
    }

    public boolean canUseContainers(ClaimRole role) {
        return role == ClaimRole.CONTAINER || canBuild(role);
    }

    public boolean canBasicInteract(ClaimRole role) {
        return role == ClaimRole.MEMBER || canUseContainers(role);
    }

    public boolean canManageFlags(ClaimRole role) {
        return role == ClaimRole.CO_OWNER || role == ClaimRole.OWNER;
    }

    public boolean canTrust(ClaimRole role) {
        return role == ClaimRole.CO_OWNER || role == ClaimRole.OWNER;
    }

    public boolean canTransfer(ClaimRole role) {
        return role == ClaimRole.OWNER;
    }
}
