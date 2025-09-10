package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Handles item drop & pickup protection inside claims.
 * - Uses both global config (protection.items) and per-claim settings
 * - Role-based checks to decide if allowed
 */
@SuppressWarnings("deprecation") // for PlayerPickupItemEvent in legacy APIs
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

        ClaimRole role = (plot != null) ? roleManager.getRole(plot, player) : ClaimRole.VISITOR;
        FileConfiguration config = plugin.getConfig();

        boolean globalAllowed = isAllowed(config, "protection.items.drop", role);
        boolean claimAllowed = (plot != null) ? plot.getSettings().canDropItems(role) : globalAllowed;

        if (!claimAllowed) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot drop items here.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);

        ClaimRole role = (plot != null) ? roleManager.getRole(plot, player) : ClaimRole.VISITOR;
        FileConfiguration config = plugin.getConfig();

        boolean globalAllowed = isAllowed(config, "protection.items.pickup", role);
        boolean claimAllowed = (plot != null) ? plot.getSettings().canPickupItems(role) : globalAllowed;

        if (!claimAllowed) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot pick up items here.");
        }
    }

    private boolean isAllowed(FileConfiguration config, String path, ClaimRole role) {
        return config.getBoolean(path + "." + role.name().toLowerCase(), true);
    }
}
