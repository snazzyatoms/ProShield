// src/main/java/com/snazzyatoms/proshield/roles/ClaimRoleManager.java
package com.snazzyatoms.proshield.roles;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * ClaimRoleManager (ProShield v1.2.6-polished)
 * -----------------------------------
 * - Central authority for claim roles & permissions
 * - Pulls default role name from config.yml
 * - Provides helpers for GUI, listeners, and protection checks
 * - Uses messages.yml for player-facing feedback
 * - Fully synced with ClaimRole.getDisplayName() (customizable/localizable)
 * - Added compatibility shims for GUIManager
 */
public class ClaimRoleManager {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    public ClaimRoleManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* ========================
     * Lifecycle (future persistence hooks)
     * ======================== */
    public void loadAll() {
        // TODO: load persisted roles if stored separately (planned for 2.0)
    }

    public void saveAll() {
        // TODO: persist roles if needed (planned for 2.0)
    }

    /* ========================
     * Core Role Utilities
     * ======================== */

    /**
     * Get a player's role inside a plot.
     * Defaults to VISITOR if not trusted/owner.
     */
    public ClaimRole getRole(UUID playerId, Plot plot) {
        if (plot == null || playerId == null) return ClaimRole.NONE;
        if (plot.getOwner().equals(playerId)) return ClaimRole.OWNER;

        ClaomRole raw = plot.getTrusted().get(playerId);
        return raw != null ? ClaimRole.fromName(raw) : ClaimRole.VISITOR;
    }

    /**
     * Assign or remove a role in a plot.
     */
    public void setRole(Plot plot, UUID targetId, ClaimRole role) {
        if (plot == null || targetId == null || role == null) return;

        if (role == ClaimRole.NONE || role == ClaimRole.VISITOR) {
            plot.getTrusted().remove(targetId);
        } else {
            plot.getTrusted().put(targetId, role.name());
        }
        plotManager.saveAll();
    }

    /* ========================
     * Permission Helpers
     * ======================== */
    public boolean canInteract(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canInteract();
    }

    public boolean canBuild(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canBuild();
    }

    public boolean canOpenContainers(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canOpenContainers();
    }

    public boolean canModifyFlags(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canModifyFlags();
    }

    public boolean canManageRoles(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canManageRoles();
    }

    public boolean canTransferClaim(Player player, Plot plot) {
        return getRole(player.getUniqueId(), plot).canTransferClaim();
    }

    /* ========================
     * Utility
     * ======================== */

    /**
     * Compare two players' hierarchy inside a plot.
     */
    public boolean hasHigherOrEqualRole(Player actor, UUID target, Plot plot) {
        ClaimRole actorRole = getRole(actor.getUniqueId(), plot);
        ClaimRole targetRole = getRole(target, plot);
        return actorRole.isAtLeast(targetRole);
    }

    /**
     * Assign a role via chat input (legacy support).
     * Uses roles.default from config.yml as fallback.
     */
    public void assignRoleViaChat(Player actor, UUID targetUuid, String rawRole) {
        if (actor == null || targetUuid == null || rawRole == null) return;

        Plot plot = plotManager.getPlotAt(actor.getLocation());
        if (plot == null) {
            messages.send(actor, messages.getOrDefault("messages.error.no-claim-here",
                    "&cYou must stand inside your claim to manage roles."));
            return;
        }

        // Only owner or admin can assign roles
        if (!plot.getOwner().equals(actor.getUniqueId()) && !actor.hasPermission("proshield.admin")) {
            messages.send(actor, messages.getOrDefault("messages.error.not-owner",
                    "&cOnly the owner (or admin) can assign roles in this claim."));
            return;
        }

        ClaimRole role = ClaimRole.fromName(rawRole);
        if (role == ClaimRole.NONE) {
            String fallback = plugin.getConfig().getString("roles.default", "member");
            role = ClaimRole.fromName(fallback);
        }

        setRole(plot, targetUuid, role);

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetUuid);
        String targetName = (target != null && target.getName() != null)
                ? target.getName()
                : targetUuid.toString().substring(0, 8);

        // ✅ Always use ClaimRole.getDisplayName() (messages.yml aware)
        messages.send(actor, messages.getOrDefault("messages.roles.assigned",
                "&aAssigned &f{player} &ato role &f{role} &ain this claim.")
                .replace("{player}", targetName)
                .replace("{role}", role.getDisplayName()));
    }

    /* ========================
     * Compatibility Shims (for GUIManager 1.2.6)
     * ======================== */

    /** GUI expects to list all roles */
    public List<ClaimRole> getAllRoles() {
        return Arrays.asList(ClaimRole.values());
    }

    /** GUI sometimes calls role lookup by raw string */
    public ClaimRole getRole(String raw) {
        return ClaimRole.fromName(raw);
    }
}
