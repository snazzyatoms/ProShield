package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<String, UUID> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadAll();
    }

    private String key(Location location) {
        return location.getWorld().getName() + ":" + location.getChunk().getX() + ":" + location.getChunk().getZ();
    }

    public boolean createClaim(UUID owner, Location location) {
        String k = key(location);
        if (claims.containsKey(k)) return false;
        claims.put(k, owner);
        plugin.getConfig().set("claims." + k, owner.toString());
        plugin.saveConfig();
        return true;
    }

    public boolean removeClaim(UUID owner, Location location) {
        String k = key(location);
        if (!claims.containsKey(k)) return false;
        if (!claims.get(k).equals(owner)) return false;
        claims.remove(k);
        plugin.getConfig().set("claims." + k, null);
        plugin.saveConfig();
        return true;
    }

    public boolean isClaimed(Location location) {
        return claims.containsKey(key(location));
    }

    public boolean isOwner(UUID owner, Location location) {
        String k = key(location);
        return claims.containsKey(k) && claims.get(k).equals(owner);
    }

    public int getClaimCount() {
        return claims.size();
    }

    public void saveAll() {
        plugin.getConfig().set("claims", null);
        for (Map.Entry<String, UUID> e : claims.entrySet()) {
            plugin.getConfig().set("claims." + e.getKey(), e.getValue().toString());
        }
        plugin.saveConfig();
    }

    private void loadAll() {
        claims.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("claims");
        if (section == null) return;
        for (String k : section.getKeys(false)) {
            String uuidStr = section.getString(k);
            if (uuidStr == null) continue;
            try {
                claims.put(k, UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid UUID in config for claim key: " + k);
            }
        }
    }
}
