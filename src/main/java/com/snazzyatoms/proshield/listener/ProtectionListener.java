package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    private Plot getPlot(Player player) {
        return plotManager.getPlot(player.getLocation());
    }

    /* ========================
     * BLOCK BREAK / PLACE
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        if (!plot.isOwner(player.getUniqueId()) && !plot.isTrusted(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "&cYou cannot break blocks in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        if (!plot.isOwner(player.getUniqueId()) && !plot.isTrusted(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "&cYou cannot place blocks in this claim.");
        }
    }

    /* ========================
     * ENTITY DAMAGE / PVP / MOBS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Protect players in claims
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
                    if (!plot.getFlag("safezone", false)) {
                        event.setCancelled(true);
                    }
                }
                // Projectile by mobs
                else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj) {
                    ProjectileSource shooter = proj.getShooter();
                    if (shooter instanceof Monster && !plot.getFlag("safezone", false)) {
                        event.setCancelled(true);
                    }
                }
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
            // Extra: remove creeper if it explodes inside claim
            if (event.getEntity() instanceof Creeper) {
                event.getEntity().remove();
            }
            // Remove TNT entities too
            if (event.getEntity() instanceof TNTPrimed) {
                event.getEntity().remove();
            }
        }
    }

    /* ========================
     * ITEM FRAME / ARMOR STAND
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;
        Plot plot = plotManager.getPlot(event.getEntity().getLocation());
        if (plot == null) return;

        if (!plot.isOwner(player.getUniqueId()) && !plot.isTrusted(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getMessagesUtil().send(player, "&cYou cannot remove that here.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Plot plot = getPlot(player);
        if (plot == null) return;

        // Check containers, frames, etc. if needed:
        if (!plot.isOwner(player.getUniqueId()) && !plot.isTrusted(player.getUniqueId())) {
            if (!plot.getFlag("interact", false)) {
                event.setCancelled(true);
                plugin.getMessagesUtil().send(player, "&cYou cannot interact here.");
            }
        }
    }
}
