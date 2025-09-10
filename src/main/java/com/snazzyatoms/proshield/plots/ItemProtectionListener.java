package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
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

/**
 * Handles item-frame, armor-stand, and container protections inside claims.
 * Extended to merge global + per-claim keep-items rules.
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

    // === Item Frames ===
    @EventHandler(ignoreCancelled = true)
    public void onItemFrameInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;

        Player player = event.getPlayer();
        Chunk chunk = frame.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness, allow

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canInteract(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with item frames here.");
        }
    }

    // === Armor Stands ===
    @EventHandler(ignoreCancelled = true)
    public void onArmorStandInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;

        Player player = event.getPlayer();
        Chunk chunk = stand.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness, allow

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canInteract(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with armor stands here.");
        }
    }

    // === Hanging Entities (Item Frames, Paintings) ===
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Chunk chunk = event.getEntity().getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot break hanging entities here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getEntity().getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot place hanging entities here.");
        }
    }

    // === Global + Per-Claim Item Keep Logic ===
    public boolean isKeepItemsEnabled(Chunk chunk) {
        FileConfiguration config = plugin.getConfig();

        // Global toggle
        boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            return globalKeep; // wilderness uses global
        }

        // Per-claim override if set, otherwise fall back to global
        Boolean claimKeep = plot.getSettings().getKeepItemsEnabled();
        return (claimKeep != null) ? claimKeep : globalKeep;
    }
}
