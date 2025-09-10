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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles item drop & pickup rules in wilderness and claims.
 * Supports "keep-items" config toggle to prevent item despawn inside claims.
 */
@SuppressWarnings("deprecation") // PlayerPickupItemEvent is deprecated but still works across 1.18–1.21
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    private boolean keepItemsEnabled;
    private int despawnSeconds;
    private boolean allowWildernessDrop;
    private boolean allowWildernessPickup;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;
        reload();
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();
        Chunk chunk = item.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        FileConfiguration config = plugin.getConfig();

        // Wilderness rules
        if (plot == null) {
            if (!allowWildernessDrop) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items in the wilderness.");
            }
            return;
        }

        // Claim rules: allow owner + trusted
        ClaimRole role = roleManager.getRole(plot, player);
        if (roleManager.canDropItems(role)) {
            return;
        }

        // If not trusted → cancel
        event.setCancelled(true);
        player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items here.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        Chunk chunk = item.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        // Wilderness rules
        if (plot == null) {
            if (!allowWildernessPickup) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items in the wilderness.");
            }
            return;
        }

        // Claim rules: allow owner + trusted
        ClaimRole role = roleManager.getRole(plot, player);
        if (roleManager.canPickupItems(role)) {
            return;
        }

        // Not trusted → block
        event.setCancelled(true);
        player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items here.");
    }

    /**
     * Keeps items inside claims alive (prevents despawn).
     * Runs a repeating task to reset their age until despawn-seconds.
     */
    public void startKeepItemsTask() {
        if (!keepItemsEnabled) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                    for (Item item : world.getEntitiesByClass(Item.class)) {
                        Chunk chunk = item.getLocation().getChunk();
                        Plot plot = plotManager.getPlot(chunk);
                        if (plot != null) {
                            item.setTicksLived(0); // reset despawn timer
                            if (despawnSeconds > 0) {
                                item.setTicksLived((20 * despawnSeconds) - 100); // ensure safe buffer
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L); // every 10s
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        this.keepItemsEnabled = config.getBoolean("claims.keep-items.enabled", false);
        this.despawnSeconds = config.getInt("claims.keep-items.despawn-seconds", 900);
        this.allowWildernessDrop = config.getBoolean("protection.wilderness.allow-item-drop", true);
        this.allowWildernessPickup = config.getBoolean("protection.wilderness.allow-item-pickup", true);
    }
}
