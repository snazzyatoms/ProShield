package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final Map<UUID, Location> claims = new HashMap<>();

    public PlotManager(Object plugin) {
        // plugin reference if needed later
    }

    // --- Preferred methods (Player-based) ---
    public boolean createClaim(Player player, Location location) {
        return createClaim(player.getUniqueId(), location);
    }

    public boolean removeClaim(Player player, Location location) {
        return removeClaim(player.getUniqueId(), location);
    }

    // --- Overloaded methods (UUID-based) ---
    public boolean createClaim(UUID playerId, Location location) {
        if (claims.containsKey(playerId)) {
            return false; // already claimed
        }
        claims.put(playerId, location);
        return true;
    }

    public boolean removeClaim(UUID playerId, Location location) {
        if (!claims.containsKey(playerId)) {
            return false; // nothing to remove
        }
        claims.remove(playerId);
        return true;
    }

    public int getClaimCount() {
        return claims.size();
    }

    public void saveAll() {
        // TODO: persist claims to config or DB
    }
}
