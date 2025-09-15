package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages trusted players & roles in claims.
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plotManager;

    // Map<claimId, Map<playerId, role>>
    private final Map<UUID, Map<UUID, String>> claimRoles = new HashMap<>();

    public ClaimRoleManager(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    public void trustPlayer(UUID claimId, UUID playerId, String role) {
        claimRoles.computeIfAbsent(claimId, k -> new HashMap<>()).put(playerId, role);
    }

    public void untrustPlayer(UUID claimId, UUID playerId) {
        Map<UUID, String> roles = claimRoles.get(claimId);
        if (roles != null) roles.remove(playerId);
    }

    public String getRole(UUID claimId, UUID playerId) {
        Map<UUID, String> roles = claimRoles.get(claimId);
        return roles != null ? roles.get(playerId) : null;
    }

    public boolean isTrusted(UUID claimId, UUID playerId) {
        Map<UUID, String> roles = claimRoles.get(claimId);
        return roles != null && roles.containsKey(playerId);
    }

    // ✅ FIX: Added assignRoleViaChat for GUIManager
    public void assignRoleViaChat(Player player, String roleName) {
        Location loc = player.getLocation();
        Plot plot = plotManager.getClaimAt(loc);

        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a claim.");
            return;
        }

        UUID claimId = plotManager.getClaimIdAt(loc);
        if (claimId == null) {
            player.sendMessage(ChatColor.RED + "Error: no claim found here.");
            return;
        }

        trustPlayer(claimId, player.getUniqueId(), roleName);
        player.sendMessage(ChatColor.GREEN + "You are now assigned role " + ChatColor.AQUA + roleName
                + ChatColor.GREEN + " in claim " + ChatColor.YELLOW + plot.getOwnerName());
    }
}
