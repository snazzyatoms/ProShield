package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles item/container/interaction protection inside claims and wilderness.
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
        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return;

        Chunk chunk = player.getLocation().getChunk();
        Plot plot = plotManager.getPlot(chunk);
        FileConfiguration config = plugin.getConfig();

        // === Wilderness ===
        if (plot == null) {
            if (!config.getBoolean("protection.wilderness.allow-interact", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with blocks or items in the wilderness.");
            }
            return;
        }

        // === Inside a claim ===
        ClaimRole role = roleManager.getRole(plot, player);

        // Check if the role has interaction permission
        if (!roleManager.canInteract(role, event.getClickedBlock(), event.getItem())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with this inside the claim.");
        }
    }
}
