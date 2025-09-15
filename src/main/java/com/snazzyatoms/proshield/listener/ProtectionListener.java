package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Map;
import java.util.UUID;

public class ProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public ProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
    }

    private Plot getPlot(Player player) {
        return plotManager.getPlot(player.getLocation());
    }

    private boolean hasPermission(Plot plot, Player player, String permKey) {
        if (plot.isOwner(player.getUniqueId())) return true;

        Map<String, Boolean> perms = roleManager.getPermissions(plot.getId(), player.getName());
        if (perms.containsKey(permKey)) return perms.get(permKey);

        // Fallback: trusted players inherit default "trusted" role
        return plot.isTrusted(player.getUniqueId());
    }

    /* ========================
     * BLOCK BREAK / PLACE
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        if (!hasPermission(plot, player, "build")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "&cYou cannot break blocks in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        if (!hasPermission(plot, player, "build")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "&cYou cannot place blocks in this claim.");
        }
    }

    /* ========================
     * ENTITY DAMAGE / PVP / MOBS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Protect players
        if (event.getEntity() instanceof Player victim) {
            Plot plot = plotManager.getPlot(victim.getLocation());
            if (plot != null) {
                // PVP
                if (event.getDamager() instanceof Player attacker) {
                    if (!plot.getFlag("pvp", false)) {
                        event.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "PVP is disabled in this claim!");
                    }
                }
                // Mob attack
                else if (event.getDamager() instanceof Monster) {
                    if (!plot.getFlag("safezone", true)) { // default true = mobs blocked
                        event.setCancelled(true);
                    }
                }
                // Projectiles from mobs
                else if (event.getDamager() instanceof Projectile proj) {
                    ProjectileSource shooter = proj.getShooter();
                    if (shooter instanceof Monster && !plot.getFlag("safezone", true)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player && event.getEntity() instanceof Monster) {
            Plot plot = plotManager.getPlot(player.getLocation());
            if (plot != null && !plot.getFlag("safezone", true)) {
                event.setCancelled(true); // mobs can’t even target players in claim
            }
        }
    }

    /* ========================
     * EXPLOSIONS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlot(event.getLocation());
        if (plot == null) return;

        if (!plot.getFlag("explosions", false)) {
            event.blockList().clear(); // no block damage
            if (event.getEntity() instanceof Creeper || event.getEntity() instanceof TNTPrimed) {
                event.getEntity().remove(); // despawn inside claim
            }
        }
    }

    /* ========================
     * ITEM FRAMES / ARMOR STANDS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(event.getEntity().getLocation());
        if (plot == null) return;

        if (!hasPermission(plot, player, "build")) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "&cYou cannot remove that here.");
        }
    }

    /* ========================
     * PLAYER INTERACT / CONTAINERS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Plot plot = getPlot(player);
        if (plot == null) return;

        if (!plot.isOwner(player.getUniqueId())) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof InventoryHolder) {
                if (!hasPermission(plot, player, "containers")) {
                    event.setCancelled(true);
                    plugin.getMessagesUtil().send(player, "&cYou cannot open containers in this claim.");
                }
            } else {
                if (!hasPermission(plot, player, "interact")) {
                    event.setCancelled(true);
                    plugin.getMessagesUtil().send(player, "&cYou cannot interact here.");
                }
            }
        }
    }
}
