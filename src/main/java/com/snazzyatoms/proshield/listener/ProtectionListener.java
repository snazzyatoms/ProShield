package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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

        // Only schedule sweeps if despawn-inside is true
        if (plugin.getConfig().getBoolean("protection.mobs.despawn-inside", true)) {
            int seconds = plugin.getConfig().getInt("protection.mobs.despawn-interval-seconds", 30);
            long interval = 20L * Math.max(5, seconds); // enforce minimum 5s
            Bukkit.getScheduler().runTaskTimer(plugin, this::sweepSafezoneMobs, interval, interval);
            plugin.getLogger().info("[ProShield] Safezone mob sweeps enabled (every " + seconds + "s).");
        } else {
            plugin.getLogger().info("[ProShield] Safezone mob sweeps disabled (spawn-prevention only).");
        }
    }

    /* ===========
     * Utilities
     * =========== */
    private Plot plotAt(Block block) {
        return (block == null) ? null : plotManager.getPlot(block.getLocation());
    }
    private Plot plotAt(Entity e) {
        return (e == null) ? null : plotManager.getPlot(e.getLocation());
    }
    private boolean isOwnerOrTrusted(Plot plot, Player p) {
        return plot != null && (plot.isOwner(p.getUniqueId()) || plot.isTrusted(p.getUniqueId()));
    }
    private boolean hasRoleOverride(UUID plotId, Player p, String key) {
        if (p == null || plotId == null) return false;
        if (plugin.getBypassing().contains(p.getUniqueId())) return true;
        Plot plot = plotManager.getPlotBy
