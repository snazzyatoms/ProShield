package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Prevents players from claiming land near spawn.
 * - Global & per-world radius
 * - Allows bypass for admins
 * - Integrated with MessagesUtil
 */
public class SpawnGuardListener implements Listener {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public SpawnGuardListener(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    /**
     * Intercepts commands like /proshield claim
     */
    @EventHandler(ignoreCancelled = true)
    public void onClaimCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();
        if (!msg.startsWith("/proshield claim") && !msg.startsWith("/claim")) return;

        Player player = event.getPlayer();
        if (player.hasPermission("proshield.bypass")) return; // admins bypass

        if (!isAllowedToClaim(player.getLocation())) {
            event.setCancelled(true);
            messages.send(player, "spawn.claim-blocked");
        }
    }

    /**
     * Intercepts compass-GUI claim attempts via right-click block
     */
    @EventHandler(ignoreCancelled = true)
    public void onCompassInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("proshield.use")) return;

        if (!isAllowedToClaim(player.getLocation()) && !player.hasPermission("proshield.bypass")) {
            event.setCancelled(true);
            messages.send(player, "spawn.claim-blocked");
        }
    }

    /**
     * Core logic: is a player allowed to claim here?
     */
    private boolean isAllowedToClaim(Location loc) {
        World world = loc.getWorld();
        if (world == null) return true;

        // Read global config
        boolean enabled = plugin.getConfig().getBoolean("spawn.block-claiming", true);
        int globalRadius = plugin.getConfig().getInt("spawn.radius", 32);

        // Per-world override
        int worldRadius = plugin.getConfig().getInt("per-world." + world.getName() + ".spawn.radius", globalRadius);
        boolean worldEnabled = plugin.getConfig().getBoolean("per-world." + world.getName() + ".spawn.block-claiming", enabled);

        if (!worldEnabled) return true;

        Location spawn = world.getSpawnLocation();
        double distance = loc.distance(spawn);

        if (distance < worldRadius) {
            messages.debug(plugin, "&cBlocked claim attempt at " + loc + " within " + worldRadius + " blocks of spawn.");
            return false;
        }
        return true;
    }
}
