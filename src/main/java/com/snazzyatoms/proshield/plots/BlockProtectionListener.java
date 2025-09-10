package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Handles block breaking & placing inside claims with role-based checks.
 * Wilderness behavior is configurable in config.yml.
 */
public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    private final boolean wildernessBreakAllowed;
    private final boolean wildernessPlaceAllowed;

    public BlockProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;

        // Load wilderness settings from config
        this.wildernessBreakAllowed = plugin.getConfig().getBoolean("protection.wilderness.allow-block-break", true);
        this.wildernessPlaceAllowed = plugin.getConfig().getBoolean("protection.wilderness.allow-block-place", true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            // Wilderness case
            if (!wildernessBreakAllowed && !player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot break blocks in the wilderness.");
            }
            return;
        }

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot break blocks in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            // Wilderness case
            if (!wildernessPlaceAllowed && !player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot place blocks in the wilderness.");
            }
            return;
        }

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot place blocks in this claim.");
        }
    }
}
