// src/main/java/com/snazzyatoms/proshield/plots/ClaimProtectionListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * ClaimProtectionListener
 * - Handles block interactions, buckets, and PvP inside claims
 * - Uses ClaimRoleManager for role-based permissions
 * - Respects claim flags (block-break, block-place, bucket-use, pvp)
 * - Provides clean denial messages and debug logs
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
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "block-break",
                "&cYou cannot break blocks here.", event, true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "block-place",
                "&cYou cannot place blocks here.", event, true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "bucket-use",
                "&cYou cannot use buckets here.", event, false);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        handleBlockAction(event.getPlayer(), event.getBlock().getLocation(), "bucket-use",
                "&cYou cannot fill buckets here.", event, false);
    }

    /**
     * Unified block + bucket handling.
     *
     * @param requiresBuild true if the action needs build permission (break/place), false if only interaction (buckets)
     */
    private void handleBlockAction(Player player, org.bukkit.Location loc, String flag,
                                   String denyMessage, org.bukkit.event.Cancellable event, boolean requiresBuild) {
        Plot plot = plotManager.getPlotAt(loc);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(player.getUniqueId(), plot);
        boolean allowed = requiresBuild ? role.canBuild() : role.canInteract();

        if (!allowed) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            messages.debug("Denied " + flag + " for " + player.getName() + " (role=" + role + ")");
            return;
        }

        if (!plot.getFlag(flag)) {
            event.setCancelled(true);
            messages.send(player, denyMessage);
            messages.debug("Denied " + flag + " for " + player.getName()
                    + " (flag disabled, role=" + role + ", plot=" + plot.getId() + ")");
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
        if (plot == null) return;

        // Respect PvP flag
        if (!plot.getFlag("pvp")) {
            ClaimRole attackerRole = roleManager.getRole(attacker.getUniqueId(), plot);
            ClaimRole victimRole = roleManager.getRole(victim.getUniqueId(), plot);

            boolean attackerAllowed = attackerRole.canInteract();
            boolean victimAllowed = victimRole.canInteract();

            // Cancel if either isn't trusted enough
            if (!attackerAllowed || !victimAllowed) {
                event.setCancelled(true);
                messages.send(attacker, "&cPvP is disabled in this claim.");
                messages.debug("Prevented PvP: " + attacker.getName() + " → " + victim.getName()
                        + " (flag=pvp=false, attacker=" + attackerRole + ", victim=" + victimRole
                        + ", plot=" + plot.getId() + ")");
            }
        }
    }
}
