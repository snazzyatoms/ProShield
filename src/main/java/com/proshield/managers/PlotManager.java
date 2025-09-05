package com.snazzyatoms.proshield.managers;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Location> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadClaims();
    }

    /**
     * Create a new claim at the player's current location.
     */
    public boolean createClaim(Player player) {
        UUID uuid = player.getUniqueId();

        if (claims.containsKey(uuid)) {
            return false; // already has a claim
        }

        Location loc = player.getLocation();
        claims.put(uuid, loc);

        saveClaimToConfig(uuid, loc);
        return true;
    }

    /**
     * Get claim info for the player.
     */
    public String getClaimInfo(Player player) {
        UUID uuid = player.getUniqueId();

        if (!claims.containsKey(uuid)) {
            return null; // no claim
        }

        Location loc = claims.get(uuid);
        return "Your claim is at world=" + loc.getWorld().getName() +
               ", X=" + loc.getBlockX() +
               ", Y=" + loc.getBlockY() +
               ", Z=" + loc.getBlockZ();
    }

    /**
     * Remove the player's claim.
     */
    public boolean removeClaim(Player player) {
        UUID uuid = player.getUniqueId();

        if (!claims.containsKey(uuid)) {
            return false;
        }

        claims.remove(uuid);

        removeClaimFromConfig(uuid);
        return true;
    }

    /**
     * Load claims from config.yml into memory.
     */
    private void loadClaims() {
        FileConfiguration config = plugin.getConfig();
        if (config.isConfigurationSection("claims")) {
            for (String key : config.getConfigurationSection("claims").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String world = config.getString("claims." + key + ".world");
                    int x = config.getInt("claims." + key + ".x");
                    int y = config.getInt("claims." + key + ".y");
                    int z = config.getInt("claims." + key + ".z");

                    Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                    claims.put(uuid, loc);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load claim for " + key);
                }
            }
        }
    }

    /**
     * Save a new claim to config.yml.
     */
    private void saveClaimToConfig(UUID uuid, Location loc) {
        FileConfiguration config = plugin.getConfig();

        config.set("claims." + uuid + ".world", loc.getWorld().getName());
        config.set("claims." + uuid + ".x", loc.getBlockX());
        config.set("claims." + uuid + ".y", loc.getBlockY());
        config.set("claims." + uuid + ".z", loc.getBlockZ());

        plugin.saveConfig();
    }

    /**
     * Remove a claim from config.yml.
     */
    private void removeClaimFromConfig(UUID uuid) {
        FileConfiguration config = plugin.getConfig();

        config.set("claims." + uuid, null);
        plugin.saveConfig();
    }
}
