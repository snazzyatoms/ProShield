// path: src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Claim> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadClaims();
    }

    public void createClaim(UUID owner, Location location) {
        String key = getKey(location);
        Claim claim = new Claim(owner, location);
        claims.put(key, claim);

        plugin.getConfig().set("claims." + key + ".owner", owner.toString());
        plugin.saveConfig();
    }

    public Claim getClaim(UUID owner, Location location) {
        return claims.get(getKey(location));
    }

    public void removeClaim(UUID owner, Location location) {
        String key = getKey(location);
        claims.remove(key);

        plugin.getConfig().set("claims." + key, null);
        plugin.saveConfig();
    }

    public boolean canModify(Player player, Location location) {
        Claim claim = claims.get(getKey(location));
        return claim == null || claim.getOwner().equals(player.getUniqueId());
    }

    public int getClaimCount() {
        return claims.size();
    }

    public void saveAll() {
        for (Map.Entry<String, Claim> entry : claims.entrySet()) {
            plugin.getConfig().set("claims." + entry.getKey() + ".owner",
                    entry.getValue().getOwner().toString());
        }
        plugin.saveConfig();
    }

    private void loadClaims() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            UUID owner = UUID.fromString(section.getString(key + ".owner"));
            // Fallback: world from server default
            Location loc = parseKeyToLocation(key);
            claims.put(key, new Claim(owner, loc));
        }
    }

    private String getKey(Location loc) {
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockZ();
    }

    private Location parseKeyToLocation(String key) {
        try {
            String[] parts = key.split("_");
            return new Location(
                    Bukkit.getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    64, // default Y level
                    Integer.parseInt(parts[2])
            );
        } catch (Exception e) {
            return null;
        }
    }
}
