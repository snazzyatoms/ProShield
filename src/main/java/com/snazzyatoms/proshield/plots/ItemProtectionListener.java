package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles item protection inside claims:
 * - Role-based pickup/drop rules
 * - Keep-items (prevent despawn inside claims if enabled)
 * - Wilderness toggle (optional restrictions outside claims)
 */
@SuppressWarnings("deprecation") // PlayerPickupItemEvent is deprecated, but still works for 1.18–1.20
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;
    private final Map<UUID, Long> protectedItems = new HashMap<>();

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            // Wilderness check
            boolean allowPickup = plugin.getConfig().getBoolean("protection.wilderness.allow-item-pickup", true);
            if (!allowPickup) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in the wilderness.");
            }
            return;
        }

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canContainer(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            // Wilderness check
            boolean allowDrop = plugin.getConfig().getBoolean("protection.wilderness.allow-item-drop", true);
            if (!allowDrop) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in the wilderness.");
            }
            return;
        }

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canContainer(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in this claim.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) return; // Wilderness: normal despawn

        boolean keepEnabled = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        if (!keepEnabled) return;

        int despawnSeconds = plugin.getConfig().getInt("claims.keep-items.despawn-seconds", 900);

        // Cancel despawn & track item as "protected"
        event.setCancelled(true);
        protectedItems.put(item.getUniqueId(), System.currentTimeMillis() + (despawnSeconds * 1000L));
    }

    /**
     * Cleanup expired protected items.
     */
    public void cleanupProtectedItems() {
        long now = System.currentTimeMillis();
        protectedItems.entrySet().removeIf(entry -> now > entry.getValue());
    }
}
