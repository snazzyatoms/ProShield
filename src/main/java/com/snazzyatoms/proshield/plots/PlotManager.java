package com.snazzyatoms.proshield.plots;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlotManager {

    private final Map<String, Location> claims = new HashMap<>();

    public PlotManager(Object plugin) {
        // plugin reference stored if needed
    }

    public boolean createClaim(Player player, Location location) {
        String key = player.getUniqueId().toString();
        if (claims.containsKey(key)) {
            return false; // already has a claim
        }
        claims.put(key, location);
        return true;
    }

    public boolean removeClaim(Player player, Location location) {
        String key = player.getUniqueId().toString();
        if (!claims.containsKey(key)) {
            return false; // no claim to remove
        }
        claims.remove(key);
        return true;
    }

    public int getClaimCount() {
        return claims.size();
    }

    public void saveAll() {
        // TODO: persist claims to config or database
    }
}
