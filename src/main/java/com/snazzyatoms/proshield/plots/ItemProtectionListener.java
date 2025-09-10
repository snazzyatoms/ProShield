package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Handles item drops and pickups inside claims.
 * - Supports per-claim overrides (allowed/blocked items, keep-drops).
 * - Falls back to global config if no claim override.
 * - Role-based: only trusted roles can bypass item restrictions.
 */
@SuppressWarnings("deprecation") // PlayerPickupItemEvent is legacy but still used in 1.18–1.20
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
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness → follow global rules only
            FileConfiguration config = plugin.getConfig();
            boolean wildernessAllow = config.getBoolean("protection.wilderness.allow-item-drop", true);
            if (!wildernessAllow) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in the wilderness.");
            }
            return;
        }

        // === Inside a claim ===
        ClaimRole role = roleManager.getRole(plot, player);
        PlotSettings settings = plot.getSettings();

        // Check role → Owners/Co-Owners can always drop
        if (roleManager.isOwnerOrCoOwner(role)) return;

        // Check per-claim allowed items
        Set<String> allowedItems = settings.getAllowedItems();
        Set<String> blockedItems = settings.getBlockedItems();

        Material type = item.getType();
        if (!allowedItems.isEmpty() && !allowedItems.contains(type.name())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop " + type.name() + " in this claim.");
            return;
        }

        if (blockedItems.contains(type.name())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cDropping " + type.name() + " is blocked in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) {
            // Wilderness → follow global rules only
            FileConfiguration config = plugin.getConfig();
            boolean wildernessAllow = config.getBoolean("protection.wilderness.allow-item-pickup", true);
            if (!wildernessAllow) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in the wilderness.");
            }
            return;
        }

        // === Inside a claim ===
        ClaimRole role = roleManager.getRole(plot, player);
        PlotSettings settings = plot.getSettings();

        // Check role → Owners/Co-Owners can always pick up
        if (roleManager.isOwnerOrCoOwner(role)) return;

        // Check per-claim allowed items
        Set<String> allowedItems = settings.getAllowedItems();
        Set<String> blockedItems = settings.getBlockedItems();

        Material type = item.getType();
        if (!allowedItems.isEmpty() && !allowedItems.contains(type.name())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up " + type.name() + " in this claim.");
            return;
        }

        if (blockedItems.contains(type.name())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cPicking up " + type.name() + " is blocked in this claim.");
        }
    }
}
