// path: src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Handles all claim storage and lookup logic for ProShield.
 */
public class PlotManager {

    private final ProShield plugin;

    // Maps a location (chunk-based claim key) to its owner UUID
    private final Map<String, UUID> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadAll();
    }

    /**
     * Generates a claim key based on chunk X, Z and world.
     */
    private String getClaimKey(Location location) {
        return location.getWorld().getName() + ":" + location.getChunk().getX() + ":" + location.getChunk().getZ();
    }

    /**
     * Create a claim for the given player at the given location.
     */
    public boolean createClaim(UUID owner, Location location) {
        String key = getClaimKey(location);
        if (claims.containsKey(key)) {
            return false; // already claimed
        }
        claims.put(key, owner);
        saveClaim(key, owner);
        return true;
    }

    /**
     * Remove a claim for the given player at the given location.
     */
    public boolean removeClaim(UUID owner, Location location) {
        String key = getClaimKey(location);
        if (!claims.containsKey(key)) {
            return false; // no claim exists
        }
        if (!claims.get(key).equals(owner)) {
            return false; // not the owner
        }
        claims.remove(key);
        removeClaimFromConfig(key);
        return true;
    }

    /**
     * Check if a location is claimed.
     */
    public boolean isClaimed(Location location) {
        String key = getClaimKey(location);
        return claims.containsKey(key);
    }

    /**
     * Check if the given player owns the claim at a location.
     */
    public boolean isOwner(UUID owner, Location location) {
        String key = getClaimKey(location);
        return claims.containsKey(key) && claims.get(key).equals(owner);
    }

    /**
     * Load all claims from config into memory.
     */
    private void loadAll() {
        claims.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("claims");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String uuidStr = section.getString(key);
                if (uuidStr != null) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        claims.put(key, uuid);
                    } catch (IllegalArgumentException ignored) {
                        plugin.getLogger().warning("Invalid UUID in config for claim: " + key);
                    }
                }
            }
        }
    }

    /**
     * Save a single claim to the config file.
     */
    private void saveClaim(String key, UUID owner) {
        plugin.getConfig().set("claims." + key, owner.toString());
        plugin.saveConfig();
    }

    /**
     * Remove a claim from the config file.
     */
    private void removeClaimFromConfig(String key) {
        plugin.getConfig().set("claims." + key, null);
        plugin.saveConfig();
    }

    /**
     * Save all claims back into config (onDisable).
     */
    public void saveAll() {
        plugin.getConfig().set("claims", null); // clear
        for (Map.Entry<String, UUID> entry : claims.entrySet()) {
            plugin.getConfig().set("claims." + entry.getKey(), entry.getValue().toString());
        }
        plugin.saveConfig();
    }

    /**
     * Get number of loaded claims.
     */
    public int getClaimCount() {
        return claims.size();
    }
}
