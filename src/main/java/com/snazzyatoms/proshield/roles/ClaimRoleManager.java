package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * ClaimRoleManager — role lookups & convenience helpers.
 * Keeps prior semantics; adds trust helpers referenced by commands.
 */
public class ClaimRoleManager {

    private final ProShield plugin;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public void reloadFromConfig() {
        // roles are embedded per-claim; nothing global to reload yet
    }

    /* -------- Lookups -------- */

    public ClaimRole getRole(Plot plot, Player player) {
        if (plot == null || player == null) return ClaimRole.VISITOR;
        if (plot.isOwner(player.getUniqueId())) return ClaimRole.OWNER;
        return plot.getRole(player.getUniqueId()).orElse(ClaimRole.VISITOR);
    }

    public boolean isOwnerOrCoOwner(ClaimRole role) {
        return role == ClaimRole.OWNER || role == ClaimRole.COOWNER;
    }

    public boolean canBuild(ClaimRole role) {
        return role == ClaimRole.BUILDER || role == ClaimRole.COOWNER || role == ClaimRole.OWNER;
    }

    public boolean canInteract(ClaimRole role) {
        return role == ClaimRole.MEMBER || role == ClaimRole.CONTAINER || canBuild(role) || isOwnerOrCoOwner(role);
    }

    /* -------- Trust helpers used by commands -------- */

    public boolean trustPlayer(Plot plot, String playerName, ClaimRole role) {
        if (plot == null || playerName == null || role == null) return false;
        OfflinePlayer off = Bukkit.getOfflinePlayer(playerName);
        UUID id = off.getUniqueId();
        plot.addTrusted(id, role);
        return true;
    }

    public boolean untrustPlayer(Plot plot, String playerName) {
        if (plot == null || playerName == null) return false;
        OfflinePlayer off = Bukkit.getOfflinePlayer(playerName);
        plot.removeTrusted(off.getUniqueId());
        return true;
    }

    public Map<UUID, ClaimRole> getTrusted(Plot plot) {
        return (plot == null ? Map.of() : plot.getTrusted());
    }
}
