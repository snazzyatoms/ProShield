// src/main/java/com/snazzyatoms/proshield/plots/ClaimProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * ClaimProtectionListener (v1.2.6)
 * - Handles block break/place, bucket use, and PvP inside claims
 * - Uses ClaimRoleManager for role-based permissions
 * - Respects claim flags and world-control defaults
 * - Denial messages + PvP rules pulled from messages.yml
 */
public class ClaimProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;

    public ClaimProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = plugin.getRoleManager();
        this.messages = plugin.getMessagesUtil();
    }

    /* -------------------------
     * BLOCK INTERACTIONS
     * ------------------------- */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "block-break", messages.getOrDefault("messages.error.block-break", "&cYou cannot break blocks here."),
                event, true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "block-place", messages.getOrDefault("messages.error.block-place", "&cYou cannot place blocks here."),
                event, true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "bucket-use", messages.getOrDefault("messages.error.bucket-empty", "&cYou cannot empty buckets here."),
                event, false);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(),
                "bucket-use", messages.getOrDefault("messages.error.bucket-fill", "&cYou cannot fill buckets here."),
                event, false);
    }

    /**
     * Unified block + bucket handling.
     *
     * @param requiresBuild true if action needs build perms, false if interaction-only
     */
    private void handleBlockAction(Player player, Location loc, String flag,
                                   String denyMessage, org.bukkit.event.Cancellable event, boolean requiresBuild) {
        Plot plot = plotManager.getPlotAt(loc);
        if (plot == null) return; // wilderness = vanilla rules

        ClaimRole role = roleManager.getRole(player.getUniqueId(), plot);
        boolean allowed = requiresBuild ? role.canBuild() : role.canInteract();

        if (!allowed) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            debug("Denied " + flag + " for " + player.getName() + " (role=" + role + ")");
            return;
        }

        if (!plot.getFlag(flag)) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            debug("Denied " + flag + " for " + player.getName()
                    + " (flag=" + flag + " disabled, role=" + role + ", plot=" + plot.getId() + ")");
        }
    }

    /* -------------------------
     * PVP HANDLING
     * ------------------------- */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Plot plot = plotManager.getPlotAt(victim.getLocation());
        if (plot == null) return; // wilderness = PvP allowed

        // Inside claim → respect PvP flag (default false → safezone)
        if (!plot.getFlag("pvp")) {
            event.setCancelled(true);
            messages.send(attacker, messages.getOrDefault("messages.error.pvp", "&cPvP is disabled in this claim."));
            debug("Prevented PvP: " + attacker.getName() + " → " + victim.getName()
                    + " (flag=pvp=false, plot=" + plot.getId() + ")");
        }
    }

    private void debug(String msg) {
        if (plugin.isDebugEnabled()) {
            plugin.getLogger().info("[ClaimProtection] " + msg);
        }
    }
}
