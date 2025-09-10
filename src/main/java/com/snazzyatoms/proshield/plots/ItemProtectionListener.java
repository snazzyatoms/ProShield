package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Handles item use & interactions inside claims.
 * Wilderness behavior is configurable in config.yml.
 */
public class ItemProtectionListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;
    private final ClaimRoleManager roleManager;

    private final boolean wildernessInteractAllowed;

    public ItemProtectionListener(ProShield plugin, PlotManager plotManager, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        this.roleManager = roleManager;

        // Load wilderness setting from config
        this.wildernessInteractAllowed = plugin.getConfig().getBoolean("protection.wilderness.allow-interact", true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Ignore off-hand to avoid duplicate events
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        Chunk chunk = player.getLocation().getChunk();

        Plot plot = plotManager.getPlot(chunk);
        if (plot == null) {
            // Wilderness case
            if (!wildernessInteractAllowed && !player.hasPermission("proshield.bypass")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + "§cYou cannot interact with items in the wilderness.");
            }
            return;
        }

        ClaimRole role = roleManager.getRole(plot, player);
        if (!roleManager.canUseItems(role)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + "§cYou cannot use items here.");
        }
    }
}
