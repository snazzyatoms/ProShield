package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Location> playerClaims;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.playerClaims = new HashMap<>();
    }

    /**
     * Create a claim at the player's current location.
     */
    public void createClaim(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerClaims.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You already have a claim!");
            return;
        }

        Location loc = player.getLocation();
        playerClaims.put(uuid, loc);
        player.sendMessage(ChatColor.GREEN + "New claim created at your current location: "
                + ChatColor.YELLOW + formatLocation(loc));
    }

    /**
     * Get information about the player's claim.
     */
    public void getClaimInfo(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerClaims.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You don't have a claim yet.");
            return;
        }

        Location loc = playerClaims.get(uuid);
        player.sendMessage(ChatColor.AQUA + "Your claim is located at: "
                + ChatColor.YELLOW + formatLocation(loc));
    }

    /**
     * Remove the player's claim.
     */
    public void removeClaim(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerClaims.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You don't have a claim to remove.");
            return;
        }

        playerClaims.remove(uuid);
        player.sendMessage(ChatColor.GREEN + "Your claim has been removed.");
    }

    /**
     * Helper to format a location as x,y,z,world.
     */
    private String formatLocation(Location loc) {
        return "X:" + loc.getBlockX() +
                " Y:" + loc.getBlockY() +
                " Z:" + loc.getBlockZ() +
                " World:" + loc.getWorld().getName();
    }
}
