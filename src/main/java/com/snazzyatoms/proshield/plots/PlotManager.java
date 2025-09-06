package com.snazzyatoms.proshield.plots;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlotManager {
    private final Map<UUID, List<Claim>> playerClaims = new HashMap<>();

    public Claim createClaim(Player player, Location corner1, Location corner2) {
        Claim claim = new Claim(player.getUniqueId(), corner1, corner2);
        playerClaims.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(claim);
        return claim;
    }

    public List<Claim> getClaims(UUID owner) {
        return playerClaims.getOrDefault(owner, Collections.emptyList());
    }

    public boolean removeClaim(Player player, Claim claim) {
        List<Claim> claims = playerClaims.get(player.getUniqueId());
        if (claims != null) {
            return claims.remove(claim);
        }
        return false;
    }

    public Claim getClaimAt(Location location) {
        for (List<Claim> claims : playerClaims.values()) {
            for (Claim claim : claims) {
                if (claim.isInside(location)) {
                    return claim;
                }
            }
        }
        return null;
    }
}
