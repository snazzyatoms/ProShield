package com.snazzyatoms.proshield.managers;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Create a new claim for a player
     */
    public void createClaim(UUID playerId, Location loc) {
        FileConfiguration config = plugin.getConfig();

        config.set("claims." + playerId + ".world", loc.getWorld().getName());
        config.set("claims." + playerId + ".x", loc.getBlockX());
        config.set("claims." + playerId + ".y", loc.getBlockY());
        config.set("claims." + playerId + ".z", loc.getBlockZ());

        plugin.saveConfig();
        Bukkit.getLogger().info("[ProShield] Claim created for " + playerId);
    }

    /**
     * Get claim info for a player
     */
    public String getClaimInfo(UUID playerId) {
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("claims." + playerId)) {
            return "No claim found for this player.";
        }

        String world = config.getString("claims." + playerId + ".world");
        int x = config.getInt("claims." + playerId + ".x");
        int y = config.getInt("claims." + playerId + ".y");
        int z = config.getInt("claims." + playerId + ".z");

        return "Claim -> World: " + world + " | Location: X=" + x + ", Y=" + y + ", Z=" + z;
    }

    /**
     * Remove a player’s claim
     */
    public void removeClaim(UUID playerId) {
        FileConfiguration config = plugin.getConfig();

        if (config.contains("claims." + playerId)) {
            config.set("claims." + playerId, null);
            plugin.saveConfig();
            Bukkit.getLogger().info("[ProShield] Claim removed for " + playerId);
        }
    }
}
