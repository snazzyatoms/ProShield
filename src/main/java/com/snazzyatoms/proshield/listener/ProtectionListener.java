package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class ProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();

        boolean despawnInside = plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true);
        int interval = plugin.getConfig().getInt("protection.mobs.despawn-interval-seconds", 30);

        if (despawnInside && interval > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    sweepHostilesInsideClaims();
                }
            }.runTaskTimer(plugin, interval * 20L, interval * 20L);
            plugin.getLogger().info("Hostile mob sweep task enabled every " + interval + "s.");
        }
    }

    private void sweepHostilesInsideClaims() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Monster monster) {
                    Location loc = monster.getLocation();
                    Plot plot = plotManager.getPlot(loc);
                    if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
                        monster.remove();
                    }
                }
            }
        }
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
     * FIRE PROTECTION
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Plot plot = plotManager.getPlot(event.getBlock().getLocation());
        if (plot == null) return;

        if (!plot.getFlag("fire", plugin.getConfig().getBoolean("claims.default-flags.fire", false))) {
            event.setCancelled(true);
        }
    }

    /* ========================
     * ENTITY DAMAGE / PVP / MOBS
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            Plot plot = plotManager.getPlot(victim.getLocation());
            if (plot != null) {
                if (event.getDamager() instanceof Player attacker) {
                    if (!plot.getFlag("pvp", plugin.getConfig().getBoolean("claims.default-flags.pvp", false))) {
                        event.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "PVP is disabled in this claim!");
                    }
                }
                else if (event.getDamager() instanceof Monster) {
                    if (plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
                        event.setCancelled(true);
                    }
                }
                else if (event.getDamager() instanceof Projectile proj) {
                    ProjectileSource shooter = proj.getShooter();
                    if (shooter instanceof Monster &&
                        plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        // Prevent harming pets inside claims
        if (event.getEntity() instanceof Tameable pet && pet.isTamed()) {
            Plot plot = plotManager.getPlot(pet.getLocation());
            if (plot != null && plot.getFlag("pets", plugin.getConfig().getBoolean("claims.default-flags.pets", true))) {
                if (event.getDamager() instanceof Player attacker) {
                    if (!plot.isOwner(attacker.getUniqueId()) && !plot.isTrusted(attacker.getUniqueId())) {
                        event.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "You cannot hurt pets in this claim!");
                    }
                }
                if (event.getDamager() instanceof Monster) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /* ========================
     * MOB SPAWN PREVENTION
     * ======================== */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            Plot plot = plotManager.getPlot(event.getLocation());
            if (plot != null && plot.getFlag("safezone", plugin.getConfig().getBoolean("claims.default-flags.safezone", true))) {
                event.setCancelled(true);
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

        if (!plot.getFlag("explosions", plugin.getConfig().getBoolean("claims.default-flags.explosions", false))) {
            event.blockList().clear();
            if (event.getEntity() instanceof Creeper || event.getEntity() instanceof TNTPrimed) {
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

        if (!plot.isOwner(player.getUniqueId()) && !plot.isTrusted(player.getUniqueId())) {
            if (!plot.getFlag("interact", plugin.getConfig().getBoolean("claims.default-flags.interact", false))) {
                event.setCancelled(true);
                plugin.getMessagesUtil().send(player, "&cYou cannot interact here.");
            }
        }
    }
}
