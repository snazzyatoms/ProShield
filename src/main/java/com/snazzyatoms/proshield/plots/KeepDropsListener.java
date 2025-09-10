package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Set;

/**
 * Handles "keep items in claims" protection.
 * - Global toggle in config.yml
 * - Per-claim override in PlotSettings
 * - Prevents item despawning if enabled
 * - Owners/Co-Owners can always override
 */
public class KeepDropsListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    public KeepDropsListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item itemEntity = event.getEntity();
        Chunk chunk = itemEntity.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();

        // === Wilderness handling ===
        if (plot == null) {
            boolean globalKeep = config.getBoolean("claims.keep-items.enabled", false);
            if (globalKeep) {
                int seconds = config.getInt("claims.keep-items.despawn-seconds", 900);
                event.setCancelled(true);
                itemEntity.setTicksLived(0);
                itemEntity.setUnlimitedLifetime(true);
                itemEntity.setCustomName("§b[Protected]");
                itemEntity.setCustomNameVisible(false);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (!itemEntity.isDead()) itemEntity.remove();
                }, seconds * 20L);
            }
            return;
        }

        // === Inside a claim ===
        PlotSettings settings = plot.getSettings();
        boolean keepDropsInClaim = settings.isKeepDropsEnabled();

        if (keepDropsInClaim) {
            int seconds = config.getInt("claims.keep-items.despawn-seconds", 900);
            event.setCancelled(true);
            itemEntity.setTicksLived(0);
            itemEntity.setUnlimitedLifetime(true);
            itemEntity.setCustomName("§b[Claim-Protected]");
            itemEntity.setCustomNameVisible(false);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!itemEntity.isDead()) itemEntity.remove();
            }, seconds * 20L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        if (plot == null) return; // wilderness handled globally

        ClaimRole role = roleManager.getRole(plot, player);
        if (roleManager.isOwnerOrCoOwner(role)) {
            // Owners always allowed, items kept if claim setting enabled
            return;
        }

        // Optional: if per-claim keep-drops disabled, we could warn players
        if (!plot.getSettings().isKeepDropsEnabled()) {
            player.sendMessage(plugin.getPrefix() + "§cDropped items in this claim may despawn normally.");
        }
    }
}
