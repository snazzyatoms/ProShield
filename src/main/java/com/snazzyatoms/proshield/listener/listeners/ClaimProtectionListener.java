package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

public class ClaimProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final MessagesUtil messages;
    private final FileConfiguration config;

    public ClaimProtectionListener(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
        this.roleManager = plugin.getRoleManager();
        this.messages = plugin.getMessagesUtil();
        this.config = plugin.getConfig();
    }

    private boolean hasPermission(Player player, Plot plot, String action) {
        if (plot == null) return true;
        if (plot.isOwner(player.getUniqueId())) return true;

        String role = plot.getTrusted().get(player.getUniqueId());
        return role != null && roleManager.can(role, action);
    }

    private boolean isSafeZone(Plot plot) {
        return plot != null && plot.getFlag("safezone", config);
    }

    private void debug(Player player, String msg) {
        if (plugin.isDebugging(player.getUniqueId())) {
            player.sendMessage("§8[DEBUG] §7" + msg);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (!hasPermission(player, plot, "block-break")) {
            event.setCancelled(true);
            messages.send(player, "&cYou cannot break blocks here.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (!hasPermission(player, plot, "block-place")) {
            event.setCancelled(true);
            messages.send(player, "&cYou cannot place blocks here.");
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (!hasPermission(player, plot, "bucket-use")) {
            event.setCancelled(true);
            messages.send(player, "&cYou cannot use buckets here.");
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (!hasPermission(player, plot, "bucket-use")) {
            event.setCancelled(true);
            messages.send(player, "&cYou cannot fill buckets here.");
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        Plot plot = plotManager.getPlotAt(victim.getLocation());
        if (plot != null && !plot.getFlag("pvp", config)) {
            event.setCancelled(true);
            messages.send(attacker, "&cPvP is disabled in this claim.");
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Plot plot = plotManager.getPlotAt(event.getLocation());
        if (plot != null && !plot.getFlag("mob-spawn", config)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player player)) return;

        Plot plot = plotManager.getPlotAt(target.getLocation());
        if (isSafeZone(plot)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        Plot plot = plotManager.getPlotAt(event.getLocation());
        if (plot != null && !plot.getFlag("explosion", config)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEndermanGrief(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman)) return;
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.getFlag("enderman-grief", config)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.getFlag("ignite", config)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getSource() == null || event.getSource().getType() != Material.FIRE) return;
        Plot plot = plotManager.getPlotAt(event.getBlock().getLocation());
        if (plot != null && !plot.getFlag("fire-spread", config)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLightningDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.LIGHTNING) return;
        if (!(event.getEntity() instanceof Player player)) return;
        Plot plot = plotManager.getPlotAt(player.getLocation());
        if (isSafeZone(plot)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Projectile)) return;
        Plot plot = plotManager.getPlotAt(victim.getLocation());
        if (isSafeZone(plot)) {
            event.setCancelled(true);
        }
    }
}
