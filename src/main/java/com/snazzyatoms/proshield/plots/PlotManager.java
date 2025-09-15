package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlotManager handles claim storage and lookup.
 * Claims are stored by chunk coordinates per world.
 */
public class PlotManager {

    private final ProShield plugin;

    // Storage: world:x:z → claimId
    private final Map<String, UUID> claimChunks = new HashMap<>();
    private final Map<UUID, Plot> claims = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public Plot getClaim(UUID claimId) {
        return claims.get(claimId);
    }

    public void addClaim(UUID claimId, Plot plot) {
        claims.put(claimId, plot);
        claimChunks.put(getKey(plot.getWorldName(), plot.getChunkX(), plot.getChunkZ()), claimId);
    }

    public void removeClaim(UUID claimId) {
        Plot plot = claims.remove(claimId);
        if (plot != null) {
            claimChunks.remove(getKey(plot.getWorldName(), plot.getChunkX(), plot.getChunkZ()));
        }
    }

    public boolean isClaimed(Location location) {
        return getClaimIdAt(location) != null;
    }

    // ✅ FIX: Added methods required by ClaimRoleManager
    public Plot getClaimAt(Location location) {
        UUID claimId = getClaimIdAt(location);
        return claimId != null ? getClaim(claimId) : null;
    }

    public UUID getClaimIdAt(Location location) {
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        String worldName = location.getWorld().getName();
        return claimChunks.get(getKey(worldName, chunkX, chunkZ));
    }

    private String getKey(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }
}
