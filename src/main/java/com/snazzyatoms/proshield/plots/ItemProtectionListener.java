package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles item-related protections (dropping, item frames, armor stands, buckets)
 * This does NOT handle item persistence/keep-drops (handled by KeepDropsListener).
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
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // wilderness = allowed

        ClaimRole role = roleManager.getRole(plot, player);
        ItemStack dropped = event.getItemDrop().getItemStack();

        // Example: block TNT, lava buckets, or other restricted items
        if (!roleManager.canBuild(role)) {
            if (dropped.getType() == Material.TNT ||
                dropped.getType() == Material.LAVA_BUCKET) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop that item inside this claim.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getRightClicked().getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);

        // Protect item frames & armor stands
        if (event.getRightClicked() instanceof ItemFrame || event.getRightClicked() instanceof ArmorStand) {
            if (!roleManager.canContainer(role)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with that here.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);

        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot empty buckets in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return;

        ClaimRole role = roleManager.getRole(plot, player);

        if (!roleManager.canBuild(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot fill buckets in this claim.");
        }
    }
}
