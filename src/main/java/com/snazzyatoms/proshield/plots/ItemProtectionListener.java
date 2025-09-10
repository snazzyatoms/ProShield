package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Handles interactions with item frames, armor stands, and item pickups inside claims.
 * - Checks global config
 * - Supports per-claim overrides
 * - Role-based protections are preserved
 */
@SuppressWarnings("deprecation") // PlayerPickupItemEvent is deprecated after 1.12, but works fine with Spigot API
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Prevents breaking of item frames and armor stands inside claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();
        boolean globalProtect = config.getBoolean("protection.entities.item-frames", true);

        // Armor stands
        if (event.getEntity() instanceof ArmorStand && config.getBoolean("protection.entities.armor-stands", true)) {
            if (plot != null && !plot.getSettings().isItemProtectionEnabled()) return; // claim allows
            if (plot == null && !globalProtect) return; // wilderness allows
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot break armor stands here.");
        }

        // Item frames
        if (event.getEntity() instanceof ItemFrame && globalProtect) {
            if (plot != null && !plot.getSettings().isItemProtectionEnabled()) return;
            if (plot == null && !globalProtect) return;
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot break item frames here.");
        }
    }

    /**
     * Prevents placement of item frames and armor stands.
     */
    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();
        boolean globalProtect = config.getBoolean("protection.entities.item-frames", true);

        if (event.getEntity() instanceof ArmorStand && config.getBoolean("protection.entities.armor-stands", true)) {
            if (plot != null && !plot.getSettings().isItemProtectionEnabled()) return;
            if (plot == null && !globalProtect) return;
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot place armor stands here.");
        }

        if (event.getEntity() instanceof ItemFrame && globalProtect) {
            if (plot != null && !plot.getSettings().isItemProtectionEnabled()) return;
            if (plot == null && !globalProtect) return;
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot place item frames here.");
        }
    }

    /**
     * Prevents players from interacting with item frames or armor stands inside claims.
     */
    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getRightClicked().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();
        boolean globalProtect = config.getBoolean("protection.entities.item-frames", true);

        if (event.getRightClicked() instanceof ArmorStand && config.getBoolean("protection.entities.armor-stands", true)) {
            if (plot != null && !plot.getSettings().isItemProtectionEnabled()) return;
            if (plot == null && !globalProtect) return;
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with armor stands here.");
        }

        if (event.getRightClicked() instanceof ItemFrame && globalProtect) {
            if (plot != null && !plot.getSettings().isItemProtectionEnabled()) return;
            if (plot == null && !globalProtect) return;
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with item frames here.");
        }
    }

    /**
     * Prevents picking up items inside claims if protection is enabled.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();
        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);

        if (plot == null && !globalKeep) return;
        if (plot != null && !plot.getSettings().isKeepItemsEnabled() && !globalKeep) return;

        // Cancel pickup → items remain protected
        event.setCancelled(true);
    }
}
