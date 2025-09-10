package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

/**
 * Handles item/block interactions (containers, buttons, doors, etc.)
 * - Role-based checks inside claims
 * - Wilderness follows config rules
 */
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL)) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return;

        Block block = event.getClickedBlock();
        Chunk chunk = block.getChunk();
        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        // === Wilderness ===
        if (plot == null) {
            if (!config.getBoolean("protection.wilderness.allow-interactions", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with blocks in the wilderness.");
            }
            return;
        }

        // === Inside claims ===
        ClaimRole role = roleManager.getRole(plot, player);
        Material type = block.getType();

        // Check if this block falls under interaction protection
        if (isProtectedInteraction(type, config)) {
            if (!roleManager.canInteract(role)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with " + type.name() + " in this claim.");
            }
        }
    }

    /**
     * Checks if the block type should be protected according to config.
     */
    private boolean isProtectedInteraction(Material type, FileConfiguration config) {
        if (!config.getBoolean("protection.interactions.enabled", true)) return false;

        // Categories (config-defined shortcuts)
        if (config.getStringList("protection.interactions.categories").contains("doors")) {
            if (type.name().endsWith("_DOOR")) return true;
        }
        if (config.getStringList("protection.interactions.categories").contains("trapdoors")) {
            if (type.name().endsWith("_TRAPDOOR")) return true;
        }
        if (config.getStringList("protection.interactions.categories").contains("fence_gates")) {
            if (type.name().endsWith("_FENCE_GATE")) return true;
        }
        if (config.getStringList("protection.interactions.categories").contains("buttons")) {
            if (type.name().endsWith("_BUTTON")) return true;
        }
        if (config.getStringList("protection.interactions.categories").contains("levers")) {
            if (type == Material.LEVER) return true;
        }
        if (config.getStringList("protection.interactions.categories").contains("pressure_plates")) {
            if (type.name().endsWith("_PRESSURE_PLATE")) return true;
        }

        // Explicit block list
        if (config.getStringList("protection.interactions.list").contains(type.name())) {
            return true;
        }

        return false;
    }
}
