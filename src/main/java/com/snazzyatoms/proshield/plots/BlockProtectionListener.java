package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Handles block breaking & placing inside claims and wilderness
 * with role-based checks and configurable wilderness toggles.
 */
public class BlockProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    // Cached config values for wilderness
    private boolean wildernessBreakAllowed;
    private boolean wildernessPlaceAllowed;

    public BlockProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        reloadConfigValues();
    }

    /**
     * Reloads wilderness config values from config.yml
     */
    public void reloadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        this.wildernessBreakAllowed = config.getBoolean("protection.wilderness.allow-block-break", true);
        this.wildernessPlaceAllowed = config.getBoolean("protection.wilderness.allow-block-place", true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness
            if (!wildernessBreakAllowed && !player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot break blocks in the wilderness.");
            }
            return;
        }

        // Inside claim
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
            // Wilderness
            if (!wildernessPlaceAllowed && !player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot place blocks in the wilderness.");
            }
            return;
        }

        // Inside claim
        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot place blocks in this claim.");
        }
    }
}
